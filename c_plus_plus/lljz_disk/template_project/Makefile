CC = gcc
CPP = gcc -E
CXX = g++
CXXCPP = g++ -E
CXXFLAGS = -O3 -Wall -Wno-deprecated -fPIC
CXXLINK=$(CXX) -g
CXXCOMPILE=$(CXX) -c -g

TBSDK_LIBRARY_PATH=/home/trxh/lib
TBSDK_LIB=$(TBSDK_LIBRARY_PATH)/lib/libtbnet.a $(TBSDK_LIBRARY_PATH)/lib/libtbsys.a
TBSDK_INC = -I$(TBSDK_LIBRARY_PATH)/include
JSONCPP_INC=-I/home/trxh/my_projects/jsoncpp/include
JSONCPP_LIB=/home/trxh/my_projects/jsoncpp/libs/linux-gcc-4.8/libjson_linux-gcc-4.8_libmt.a
RAPIDJSON_INC=-I/home/trxh/my_projects/rapidjson/include
RAPIDJSON_LIB=
JSON_INC=$(RAPIDJSON_INC)
JSON_LIB=$(RAPIDJSON_LIB)

#TBSDK_LIBRARY_PATH=/home/lljz/lib
#TBSDK_LIB=$(TBSDK_LIBRARY_PATH)/lib/libtbnet.a $(TBSDK_LIBRARY_PATH)/lib/libtbsys.a
#TBSDK_INC = -I$(TBSDK_LIBRARY_PATH)/include
#JSONCPP_INC=-I/home/lljz/projects/jsoncpp-master/include
#JSONCPP_LIB=/home/lljz/projects/jsoncpp-master/libs/linux-gcc-4.9.2/libjson_linux-gcc-4.9.2_libmt.a
#RAPIDJSON_INC=-I/home/lljz/my_projects/rapidjson/include
#RAPIDJSON_LIB=
#JSON_INC=$(RAPIDJSON_INC)
#JSON_LIB=$(RAPIDJSON_LIB)

PROJECT_INC=-I../packets -I../protocol -I../utility -I../public_handler
VPATH=../packets ../utility ../public_handler

OUTPUT_PATH=../../output/TEMPLATE_SERVER

all:TEMPLATE_SERVER
.PHONY:TEMPLATE_SERVER

TEMPLATE_SERVER:main.o TEMPLATE_SERVER.o global.o connection_manager_from_client.o connection_manager_to_server.o load_connection_manager.o packet_handler_to_server.o handler_router.o public_echo_test_req_handler.o
	cd $(OUTPUT_PATH) && \
	$(CXXLINK) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(TBSDK_LIB) $(JSON_LIB) -lpthread -lrt

main.o:main.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(TBSDK_LIB) $(PROJECT_INC) -lpthread -lrt

#TEMPLATE_SERVER.o:TEMPLATE_SERVER.cpp TEMPLATE_SERVER.hpp base_packet.hpp packet_factory.hpp request_packet.hpp response_packet.hpp
TEMPLATE_SERVER.o:TEMPLATE_SERVER.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)

global.o:global.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)

connection_manager_to_server.o:connection_manager_to_server.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

connection_manager_from_client.o:connection_manager_from_client.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ $^ \
		$(TBSDK_INC) $(PROJECT_INC) $(JSON_INC)

load_connection_manager.o:load_connection_manager.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)

packet_handler_to_server.o:packet_handler_to_server.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)

handler_router.o:handler_router.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)

public_echo_test_req_handler.o:public_echo_test_req_handler.cpp
	$(CXXCOMPILE) $(CXXFLAGS) -o $(OUTPUT_PATH)/$@ \
		$^ $(TBSDK_INC) $(PROJECT_INC)
