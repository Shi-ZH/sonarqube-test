#!/usr/bin/env python3

import datetime
import sys
import socket
import threading
import time

UUID = ""
NAME = ""
BACKEND_PORT = 0
PEER_COUNT = 0
BUFF_SIZE = 1024
SEQUENCE_NUM = 0
neighbor_nodes = dict()  # store information of all neighbor nodes
inactive = list()  # mark inactive neighbor nodes
keep_alive_time = dict()  # store the time of last received keep-alive message
nodes_name = dict()
nodes_sequence = dict()
nodes_map = dict()
routing_table = list()
SOCKET_CLIENT = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
SOCKET_SERVER = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)


def read_neighbors(line_info):
    info_temp = line_info.rstrip().replace(" ", "").split("=")[1]
    info = info_temp.split(",")
    uuid = info[0]
    host = info[1]
    port = int(info[2])
    metric = int(info[3])
    if type(uuid) == str and type(host) == str and type(port) == int and type(metric) == int:
        keys = ["uuid", "host", "backend_port", "metric"]
        values = [uuid, host, port, metric]
        neighbor_nodes[uuid] = dict(zip(keys, values))


def initialize():
    nodes_name[UUID] = NAME

    source = dict()
    for neighbor_uuid in neighbor_nodes.keys():
        neighbor_metric = neighbor_nodes.get(neighbor_uuid).get("metric")
        source[neighbor_uuid] = neighbor_metric
    nodes_map[UUID] = source


def get_uuid():
    response = {"uuid": UUID}
    print(response)


def get_neighbors():
    active_list = list()
    name_list = list()
    for neighbor_uuid in neighbor_nodes.keys():
        if neighbor_uuid not in inactive:
            active_list.append(neighbor_nodes.get(neighbor_uuid))
            if neighbor_uuid in nodes_name.keys():
                name_list.append(nodes_name.get(neighbor_uuid))
            else:
                name_list.append(neighbor_uuid)
    nodes_dict = dict(zip(name_list, active_list))
    response = {"neighbors": nodes_dict}
    print(response)


def get_map():
    whole_map = dict()
    for source_uuid in nodes_map.keys():
        if source_uuid in inactive:
            continue
        source_name = nodes_name.get(source_uuid)
        source_node = dict()
        for neighbor_uuid in nodes_map.get(source_uuid).keys():
            if neighbor_uuid in inactive:
                continue
            neighbor_name = nodes_name.get(neighbor_uuid)
            neighbor_metric = nodes_map.get(source_uuid).get(neighbor_uuid)
            source_node[neighbor_name] = neighbor_metric
        whole_map[source_name] = source_node
    response = {"map": whole_map}
    print(response)


def get_rank():
    rank = dict()
    for node in routing_table:
        if node.get("uuid") in inactive:
            continue
        node_name = nodes_name.get(node.get("uuid"))
        if node_name:
            rank[node_name] = node.get("metric")
    response = {"rank": rank}
    print(response)


def add_neighbor(cmd):
    cmd_arg = cmd.rstrip().split(" ")
    cmd_arg.pop(0)
    cmd_arg[0] = (cmd_arg[0].split("="))[1]
    cmd_arg[1] = (cmd_arg[1].split("="))[1]
    cmd_arg[2] = int((cmd_arg[2].split("="))[1])
    cmd_arg[3] = int((cmd_arg[3].split("="))[1])
    uuid = cmd_arg[0]
    metric = cmd_arg[3]
    keys = ["uuid", "host", "backend_port", "metric"]
    if uuid not in neighbor_nodes.keys():
        neighbor_nodes[uuid] = dict(zip(keys, cmd_arg))
        nodes_map[UUID][uuid] = metric

        # send lsa because a new neighbor is added
        global SEQUENCE_NUM
        lsa_message = "lsa," + UUID + "," + NAME + "," + str(SEQUENCE_NUM)
        for neighbor_uuid in neighbor_nodes:
            if neighbor_uuid not in inactive:
                neighbor_metric = neighbor_nodes.get(neighbor_uuid).get("metric")
                lsa_message = lsa_message + ";" + neighbor_uuid + "," + str(neighbor_metric)
        SEQUENCE_NUM += 1
        forward_message(lsa_message)


def udp_server(stop):
    # bind socket
    SOCKET_SERVER.bind(("127.0.0.1", BACKEND_PORT))

    while True:
        if stop():
            break
        # accept a packet
        datagram, address = SOCKET_SERVER.recvfrom(BUFF_SIZE)
        datagram = datagram.decode()
        if datagram.startswith("keep-alive"):
            receive_keepalive(datagram)
        if datagram.startswith("lsa"):
            receive_lsa(datagram)


def send_keepalive(stop):
    while True:
        if stop():
            break

        curr_time = datetime.datetime.now()
        detect_reachable(curr_time)
        for neighbor_uuid in neighbor_nodes.keys():
            if neighbor_uuid not in inactive:
                neighbor_address = neighbor_nodes.get(neighbor_uuid).get("host")
                neighbor_port = neighbor_nodes.get(neighbor_uuid).get("backend_port")
                neighbor_metric = neighbor_nodes.get(neighbor_uuid).get("metric")
                ka_message = "keep-alive," + curr_time.strftime('%Y-%m-%d %H:%M:%S') + "," + UUID + "," + "127.0.0.1," \
                             + str(BACKEND_PORT) + "," + str(neighbor_metric)
                SOCKET_CLIENT.sendto(ka_message.encode(), ("127.0.0.1", neighbor_port))

        time.sleep(5)


def receive_keepalive(datagram):
    data = datagram.split(",")
    receive_time = datetime.datetime.strptime(data[1], '%Y-%m-%d %H:%M:%S')
    uuid = data[2]
    keep_alive_time[uuid] = receive_time
    if uuid in inactive:
        inactive.remove(uuid)

    # add new neighbor (automatically detect)
    if uuid not in neighbor_nodes.keys():
        host = data[3]
        backend_port = data[4]
        metric = data[5]
        cmd = "addneighbor uuid=" + uuid + " host=" + host + " backend_port=" \
              + backend_port + " metric=" + metric + "\n"
        add_neighbor(cmd)


def detect_reachable(curr_time):
    for uuid in keep_alive_time.keys():
        if uuid == UUID:
            continue
        last_time = keep_alive_time.get(uuid)
        if (curr_time - last_time).seconds > 15:
            if uuid not in inactive:
                # mark this node as inactive
                inactive.append(uuid)


def send_lsa(stop):
    while True:
        if stop():
            break

        global SEQUENCE_NUM
        # generate lsa message
        lsa_message = "lsa," + UUID + "," + NAME + "," + str(SEQUENCE_NUM)
        for neighbor_uuid in neighbor_nodes.keys():
            if neighbor_uuid not in inactive:
                neighbor_metric = neighbor_nodes.get(neighbor_uuid).get("metric")
                lsa_message = lsa_message + ";" + neighbor_uuid + "," + str(neighbor_metric)
        SEQUENCE_NUM += 1
        # send lsa message to all neighbors
        forward_message(lsa_message)

        time.sleep(5)


def receive_lsa(datagram):
    data = datagram.split(";")
    source = data[0].split(",")
    source_uuid = source[1]
    source_name = source[2]
    source_seq = source[3]
    receive_time = datetime.datetime.now()
    keep_alive_time[source_uuid] = receive_time

    if source_uuid == UUID:
        return

    if source_uuid not in nodes_name.keys():
        nodes_name[source_uuid] = source_name

    if source_uuid not in nodes_sequence.keys():
        nodes_sequence[source_uuid] = int(source_seq)
    else:
        if int(source_seq) <= nodes_sequence.get(source_uuid):
            return
        else:
            nodes_sequence[source_uuid] = int(source_seq)

    # forward a copy to its neighbors
    forward_message(datagram)

    # update network map
    node_map = dict()
    for i in range(1, len(data)):
        node_data = data[i].split(",")
        uuid = node_data[0]
        metric = int(node_data[1])
        node_map[uuid] = metric
    nodes_map[source_uuid] = node_map

    global routing_table
    routing_table = dijkstra()


def forward_message(message):
    for neighbor_uuid in neighbor_nodes:
        if neighbor_uuid not in inactive:
            neighbor_address = neighbor_nodes.get(neighbor_uuid).get("host")
            neighbor_port = neighbor_nodes.get(neighbor_uuid).get("backend_port")
            SOCKET_CLIENT.sendto(message.encode(), ("127.0.0.1", neighbor_port))


def dijkstra():
    routing = list()
    done = list()
    done_visited = list()
    horizon = list()
    horizon_visited = list()
    done.append({"uuid": UUID, "metric": 0})
    done_visited.append(UUID)
    for uuid in nodes_map.get(UUID).keys():
        if uuid not in inactive:
            metric = nodes_map.get(UUID).get(uuid)
            horizon.append({"uuid": uuid, "metric": metric})
            horizon_visited.append(uuid)

    while len(horizon) != 0:
        horizon.sort(key=lambda x: (x["metric"]))
        selected = horizon.pop(0)
        sel_uuid = selected.get("uuid")
        sel_metric = selected.get("metric")

        routing.append({"uuid": sel_uuid, "metric": sel_metric})

        horizon_visited.remove(sel_uuid)
        done.append(selected)
        done_visited.append(sel_uuid)

        # add new nodes into horizon
        if sel_uuid not in nodes_map.keys():
            break
        curr_dict = nodes_map.get(sel_uuid)
        for curr_uuid in curr_dict.keys():
            if curr_uuid in inactive:
                continue
            elif curr_uuid in done_visited:
                continue
            # update total metric of a node
            elif curr_uuid in horizon_visited:
                for item in horizon:
                    if item.get("uuid") == curr_uuid:
                        prev_metric = item.get("metric")
                        curr_metric = sel_metric + curr_dict.get(curr_uuid)
                        if curr_metric < prev_metric:
                            horizon.remove(item)
                            horizon.append({"uuid": curr_uuid, "metric": curr_metric})
                        break
            else:
                curr_metric = sel_metric + curr_dict.get(curr_uuid)
                horizon.append({"uuid": curr_uuid, "metric": curr_metric})
                horizon_visited.append(curr_uuid)

    return routing


if __name__ == '__main__':

    config_file = sys.argv[2]

    # read config file
    f = open(config_file, "r")
    for line in f:
        if line.startswith("uuid"):
            UUID = line.rstrip().replace(" ", "").split("=")[1]
        elif line.startswith("name"):
            NAME = line.rstrip().replace(" ", "").split("=")[1]
        elif line.startswith("backend_port"):
            BACKEND_PORT = int(line.rstrip().replace(" ", "").split("=")[1])
        elif line.startswith("peer_count"):
            PEER_COUNT = int(line.rstrip().replace(" ", "").split("=")[1])
        else:
            read_neighbors(line)
    initialize()

    thread_stop = False
    thread_keepalive = threading.Thread(target=send_keepalive, args=(lambda: thread_stop, ))
    thread_lsa = threading.Thread(target=send_lsa, args=(lambda: thread_stop, ))
    thread_server = threading.Thread(target=udp_server, args=(lambda: thread_stop, ))
    thread_keepalive.start()
    thread_lsa.start()
    thread_server.start()

    while True:
        command = input()
        if command == "uuid":
            get_uuid()

        if command == "neighbors":
            get_neighbors()

        if command.startswith("addneighbor"):
            add_neighbor(command)

        if command == "map":
            get_map()

        if command == "rank":
            get_rank()

        if command == "kill":
            thread_stop = True
            thread_keepalive.join()
            thread_lsa.join()
            thread_server.join()
            SOCKET_CLIENT.close()
            SOCKET_SERVER.close()
            sys.exit(0)
