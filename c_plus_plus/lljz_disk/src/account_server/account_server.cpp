#include "account_server.hpp"
#include "request_packet.hpp"
#include "response_packet.hpp"
#include "account_server_handler.h"
#include "handler_router.hpp"
#include "redis_client.h"
#include "redis_client_manager.h"

using namespace tbnet;

namespace lljz {
namespace disk {

AccountServer::AccountServer()
:conn_manager_to_srv_(NULL) {
    clientDisconnThrowPackets_=0;
    queueThreadTimeoutThrowPackets_=0;
}

AccountServer::~AccountServer() {
}

void AccountServer::Start() {
    if (Initialize()) {
        return;
    }

    if (!InitHandler()) {
        TBSYS_LOG(ERROR,"%s","init handler start error");
        return;
    }

    //process thread
    task_queue_thread_.start();
    conn_manager_from_client_.start();

    conn_manager_to_srv_=new ConnectionManagerToServer(
        &to_server_transport_,&packet_streamer_,this);
    
    if (!conn_manager_to_srv_->start()) {
        TBSYS_LOG(ERROR,"%s","conn_manager_to_srv start error");
        Stop();
        return;
    }

    //transport
    char spec[32];
    bool ret=true;
    if (ret) {
        int port=TBSYS_CONFIG.getInt("server","port",10010);
        sprintf(spec,"tcp::%d",port);
        if (from_client_transport_.listen(spec, &packet_streamer_,this)==NULL) {
            TBSYS_LOG(ERROR,"listen port %d error",port);
            ret=false;
        } else {
            TBSYS_LOG(INFO,"listen tcp port: %d",port);
        }
    }

    if (ret) {
        TBSYS_LOG(INFO,"--- program stated PID: %d ---",getpid());
        from_client_transport_.start();
        to_server_transport_.start();
    } else {
        Stop();
    }

    task_queue_thread_.wait();
    conn_manager_from_client_.wait();
    conn_manager_to_srv_->wait();
    from_client_transport_.wait();
    to_server_transport_.wait();
    WaitHandler();
    Destroy();
}

void AccountServer::Stop() {
    task_queue_thread_.stop();
    conn_manager_from_client_.stop();
    conn_manager_to_srv_->stop();
    from_client_transport_.stop();
    to_server_transport_.stop();
    StopHandler();
}

int AccountServer::Initialize() {
    //packet_streamer
    packet_streamer_.setPacketFactory(&packet_factory_);

    int thread_count = TBSYS_CONFIG.getInt(
        "server","from_client_work_thread_count",4);
    task_queue_thread_.setThreadParameter(thread_count,this,NULL);
    return EXIT_SUCCESS;
}

int AccountServer::Destroy() {
    if (conn_manager_to_srv_) {
        delete conn_manager_to_srv_;
    }
    return EXIT_SUCCESS;
}

tbnet::IPacketHandler::HPRetCode 
AccountServer::handlePacket(
tbnet::Connection *connection, 
tbnet::Packet *packet) {
    if (!packet->isRegularPacket()) {
        TBSYS_LOG(ERROR,"ControlPacket, cmd: %d",
            ((tbnet::ControlPacket* )packet)->getCommand());

        return IPacketHandler::FREE_CHANNEL;
    }

    BasePacket* bp=(BasePacket* )packet;
    bp->set_recv_time(tbsys::CTimeUtil::getTime());
    bp->set_connection(connection);
    bp->set_direction(DIRECTION_RECEIVE);
    task_queue_thread_.push(bp);

    return tbnet::IPacketHandler::KEEP_CHANNEL;
}

bool AccountServer::handlePacketQueue(
tbnet::Packet * apacket, void *args) {
    BasePacket *packet = (BasePacket *) apacket;
    tbnet::Connection* conn=packet->get_connection();
    int64_t now_time=tbsys::CTimeUtil::getTime();

    if (now_time - packet->get_recv_time() > 
        PACKET_IN_PACKET_QUEUE_THREAD_MAX_TIME) {
        //PacketQueueThread中排队3min的请求丢弃
        queueThreadTimeoutThrowPackets_++;
        TBSYS_LOG(DEBUG,"queueThreadTimeoutThrowPackets_=%d",
            queueThreadTimeoutThrowPackets_);
        return true;
    }

    if (conn==NULL || conn->isConnectState()==false) {
        //失效连接上的请求丢弃
        //避免失效连接上的业务处理消耗大量的时间
        //来自接入服务，继续处理
        clientDisconnThrowPackets_++;
        TBSYS_LOG(DEBUG,"clientDisconnThrowPackets_=%d",
            clientDisconnThrowPackets_);
        return true;
    }

    int pcode = apacket->getPCode();
    switch (pcode) {
        case REQUEST_PACKET: {
            RequestPacket *req = (RequestPacket *)packet;
            TBSYS_LOG(DEBUG,"req :chanid=%u|pcode=%u|msg_id=%u|src_type=%u|"
                "src_id=%llu|dest_type=%u|dest_id=%u|data=%s",
                req->getChannelId(),req->getPCode(),req->msg_id_,
                req->src_type_,req->src_id_,req->dest_type_,
                req->dest_id_,req->data_);

            //检查注册状态
            ResponsePacket* resp=new ResponsePacket();
            if (NULL==resp) {
                return true;
            }
            resp->setChannelId(req->getChannelId());
            resp->src_type_=req->dest_type_;
            resp->src_id_=req->dest_id_;
            resp->dest_type_=req->src_type_;
            resp->dest_id_=req->src_id_;
            resp->msg_id_=req->msg_id_+1;
            resp->error_code_=0;
            
            if (PUBLIC_REGISTER_REQ==req->msg_id_) {
                if (false == conn_manager_from_client_.IsSupportServerType(
                        req->src_type_)) {
                    resp->error_code_=3;
                    TBSYS_LOG(DEBUG,"%s","server_type not supported");
                }
                if (0!= conn_manager_from_client_.Register(
                        req->src_id_, conn)) {
                    resp->error_code_=1;
                    TBSYS_LOG(DEBUG,"%s","register fail");
                }
                if (false==conn->postPacket(resp)) {
                    delete resp;
                }
                return true;
            }

            if (false==conn_manager_from_client_.IsRegister(
                            req->src_id_)) {
                resp->error_code_=2;
                if (false==conn->postPacket(resp)) {
                    delete resp;
                }
                TBSYS_LOG(DEBUG,"%s","not register");
                return true;
            }
            //普通业务处理
            resp->set_recv_time(now_time);
            Handler handler=HANDLER_ROUTER.GetHandler(
                req->msg_id_);
            if (NULL==handler) {
                resp->error_code_=4;
            } else {
                handler(req,NULL,resp);
            }

            TBSYS_LOG(DEBUG,"resp:chanid=%u|pcode=%u|msg_id=%u|src_type=%u|"
                "src_id=%u|dest_type=%u|dest_id=%llu|error_code=%u|data=%s",
                resp->getChannelId(),resp->getPCode(),resp->msg_id_,
                resp->src_type_,resp->src_id_,resp->dest_type_,
                resp->dest_id_,resp->error_code_,resp->data_);

            conn_manager_from_client_.PostPacket(
                resp->dest_id_, resp);
            return true;
        }
        break;

        case RESPONSE_PACKET: {
            TBSYS_LOG(DEBUG,"%s","RESPONSE_PACKET");
        }
        break;
    }
    return true;
}

//收到业务应答包事件处理
bool AccountServer::BusinessHandlePacket(
tbnet::Packet *packet, void *args) {
    TBSYS_LOG(DEBUG,"%s",
        "AccountServer::BusinessHandlePacket");
    ResponsePacket* resp=NULL;
    tbnet::Connection* conn=NULL;
    int64_t now_time=tbsys::CTimeUtil::getTime();
    if (!packet->isRegularPacket()) { // 是否正常的包
        tbnet::ControlPacket* ctrl_packet=(tbnet::ControlPacket* )packet;
        int cmd=ctrl_packet->getCommand();
        if (tbnet::ControlPacket::CMD_DISCONN_PACKET==cmd) {
            assert(false);
        } else if (tbnet::ControlPacket::CMD_TIMEOUT_PACKET==cmd) {
        }
        return IPacketHandler::FREE_CHANNEL;
    }

/*    
    resp=new ResponsePacket();
    if (false==conn->postPacket(resp)) {
        delete resp;
    }
*/
    return true;
}


}
}
