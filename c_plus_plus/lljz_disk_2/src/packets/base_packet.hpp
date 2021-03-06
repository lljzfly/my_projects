/*
 * (C) 2007-2010 Alibaba Group Holding Limited
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * packet code and base packet are defined here
 *
 * Version: $Id: base_packet.hpp 998 2012-07-20 05:45:55Z zongdai $
 *
 * Authors:
 *   ruohai <ruohai@taobao.com>
 *     - initial release
 *
 */
#ifndef LLJZ_DISK_PACKETS_BASE_H
#define LLJZ_DISK_PACKETS_BASE_H
#include <string>
#include <map>
#include <set>
#include <vector>
#include <tbsys.h>
#include <tbnet.h>
#include <stdint.h>
#include <zlib.h>
#include "protocol.hpp"

using namespace std;

namespace lljz {
namespace disk {

   #define PACKET_IN_PACKET_QUEUE_THREAD_MAX_TIME  60000000   //60s
   #define PACKET_WAIT_FOR_SERVER_MAX_TIME         60000000   //60s

   //Packet type
   enum PacketType {
      REQUEST_PACKET=0,
      RESPONSE_PACKET=1
   };

   enum IODirection {
      IO_DIRECTION_RECEIVE = 1,
      IO_DIRECTION_SEND
   };

   class BasePacket : public tbnet::Packet {
   public:
      BasePacket() {
         connection_ = NULL;
         direction_ = IO_DIRECTION_SEND;
         no_free_=false;
         recv_time_ = 0;
         args_=NULL;
      }

      virtual ~BasePacket() {
      }

      // Connection
      tbnet::Connection* get_connection() {
         return connection_;
      }

      // connection
      void set_connection(tbnet::Connection *connection) {
         connection_ = connection;
      }

      // direction
      void set_direction(int direction) {
         direction_ = direction;
      }

      void* get_args() {
         return args_;
      }

      void set_args(void* args) {
         args_=args;
      }

      // direction
      int get_direction() {
         return direction_;
      }

      // recv_time
      void set_recv_time(uint64_t recv_time) {
         recv_time_=recv_time;
      }

      uint64_t get_recv_time() {
         return recv_time_;
      }

      void free() {
         //TBSYS_LOG(TRACE,"BasePacket::free:addr=%u,no_free_=%d",this,no_free_);
         if (!no_free_) {
            delete this;
         }
      }

      void set_no_free() {
         no_free_ = true;
      }

   private:
      BasePacket& operator = (const BasePacket&);

      tbnet::Connection *connection_;
      int direction_;
      bool no_free_;
      void* args_;//作为客户端，handlePacket(,args)
   protected:
      int64_t recv_time_;
   };

}
}
#endif
