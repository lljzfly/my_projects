CC = gcc
CPP = gcc -E
CXX = g++
CXXCPP = g++ -E
CXXFLAGS = -O3 -Wall -Wno-deprecated -fPIC
CXXLINK=$(CXX) -g
CXXCOMPILE=$(CXX) -c -g

#TBSDK_LIBRARY_PATH=/home/trxh/lib
#TBSDK_LIB=/home/trxh/tfs_bin/lib/libtfsclient.a $(TBSDK_LIBRARY_PATH)/lib/libtbnet.a $(TBSDK_LIBRARY_PATH)/lib/libtbsys.a
#TBSDK_INC = -I$(TBSDK_LIBRARY_PATH)/include -I$(TBSDK_LIBRARY_PATH)/include/tbnet -I$(TBSDK_LIBRARY_PATH)/include/tbsys -I/home/trxh/tfs_bin/include
#JSONCPP_INC=-I/home/trxh/my_projects/jsoncpp/include
#JSONCPP_LIB=/home/trxh/my_projects/jsoncpp/libs/linux-gcc-4.8/libjson_linux-gcc-4.8_libmt.a
#RAPIDJSON_INC=-I/home/trxh/my_projects/rapidjson/include
#RAPIDJSON_LIB=
#JSON_INC=$(RAPIDJSON_INC)
#JSON_LIB=$(RAPIDJSON_LIB)
#REDIS_INC=-I/usr/local/include/hiredis
#REDIS_LIB=/usr/local/lib/libhiredis.a

TBSDK_LIBRARY_PATH=/home/lljz/lib
TBSDK_LIB=/home/lljz/lib/tfs_client/lib/libtfsclient.a $(TBSDK_LIBRARY_PATH)/lib/libtbnet.a $(TBSDK_LIBRARY_PATH)/lib/libtbsys.a
TBSDK_INC = -I$(TBSDK_LIBRARY_PATH)/include -I$(TBSDK_LIBRARY_PATH)/include/tbnet -I$(TBSDK_LIBRARY_PATH)/include/tbsys -I/home/lljz/lib/tfs_client/include
JSONCPP_INC=-I/home/lljz/projects/jsoncpp-master/include
JSONCPP_LIB=/home/lljz/projects/jsoncpp-master/libs/linux-gcc-4.9.2/libjson_linux-gcc-4.9.2_libmt.a
RAPIDJSON_INC=-I/home/lljz/my_projects/rapidjson/include
RAPIDJSON_LIB=
JSON_INC=$(RAPIDJSON_INC)
JSON_LIB=$(RAPIDJSON_LIB)
REDIS_INC=-I/home/lljz/lib/hiredis/include
REDIS_LIB=/home/lljz/lib/hiredis/lib/libhiredis.a

PROJECT_INC=-I../packets -I../protocol -I../utility -I../public_handler -I./file_server_handler -I../redis
VPATH=../packets ../utility ./file_server_handler ../public_handler ../redis

#OUTPUT_PATH=../../output/file_server
OUTPUT_PATH=./

all:file_server
.PHONY:file_server

file_server:main.o file_server.o global.o connection_manager_to_server.o \
load_connection_manager.o packet_handler_to_server.o handler_router.o \
public_echo_test_req_handler.o redis_client.o redis_client_manager.o \
file_server_handler.o create_folder_req.o connection_manager_from_client.o \
modify_property_req.o upload_file_req.o download_file_req.o download_file_get_info_req.o
#download_file_by_seq_no_req.o
	cd $(OUTPUT_PATH) && \
	$(CXXLINK) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(TBSDK_LIB) $(JSON_LIB) \
		$(REDIS_LIB) -lpthread -lrt

main.o:main.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(TBSDK_LIB) $(PROJECT_INC) $(JSON_INC) -lpthread -lrt

#file_server.o:file_server.cpp file_server.hpp base_packet.hpp packet_factory.hpp request_packet.hpp response_packet.hpp
file_server.o:file_server.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) $(REDIS_INC)

global.o:global.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

connection_manager_to_server.o:connection_manager_to_server.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

connection_manager_from_client.o:connection_manager_from_client.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

load_connection_manager.o:load_connection_manager.cpp
	echo "compile load_connection_manager.cpp"
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

packet_handler_to_server.o:packet_handler_to_server.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

handler_router.o:handler_router.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

public_echo_test_req_handler.o:public_echo_test_req_handler.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

redis_client_manager.o:redis_client_manager.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

redis_client.o:redis_client.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

file_server_handler.o:file_server_handler.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

create_folder_req.o:create_folder_req.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

modify_property_req.o:modify_property_req.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

upload_file_req.o:upload_file_req.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

download_file_req.o:download_file_req.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

download_file_get_info_req.o:download_file_get_info_req.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
		$(REDIS_INC)

#download_file_by_seq_no_req.o:download_file_by_seq_no_req.cpp
#	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
#		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC) \
#		$(REDIS_INC)


