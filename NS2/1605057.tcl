# simulator
set ns [new Simulator]

#CBR settings
set cbr_size 100 ; #[lindex $argv 2]; #4,8,16,32,64
set cbr_rate 1.0Mb
set cbr_pckt_per_sec 10
set cbr_interval [expr 1.0/$cbr_pckt_per_sec]
# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type
set val(mac)          Mac/802_11               ;# MAC type
set val(rp)           AODV                     ;# ad-hoc routing protocol 
set val(nn)           40                       ;# number of mobilenodes
# =======================================================================

set x_dim 500 ; #[lindex $argv 1]
set y_dim 500 ;
#set time_duration 25 ; #[lindex $argv 5] ;#50
#set start_time 50 ;
#set extra_time 10

# trace file
set trace_file [open trace.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open animation.nam w]
$ns namtrace-all-wireless $nam_file $x_dim $y_dim

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $x_dim $y_dim ;# 500m x 500m area


# general operation director for mobilenodes
create-god $val(nn)


# node configs
# ======================================================================

# $ns node-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -topoInstance $topo \
                -channelType $val(chan) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF


set rn [expr $x_dim-1]

# create nodes
for {set i 0} {$i < $val(nn) } {incr i} {
    set node($i) [$ns node]
    $node($i) random-motion 1       ;# disable random motion


    set x_pos [expr int($rn*rand()+1)] ;#random settings
	set y_pos [expr int($rn*rand()+1)] ;#random settings

    $node($i) set X_ $x_pos
    $node($i) set Y_ $y_pos
    $node($i) set Z_ 0


    $ns initial_node_pos $node($i) 20

} 



# Traffic
set val(nf)         50                ;# number of flows

for {set i 0} {$i < $val(nf)} {incr i} {

    set src [expr int($val(nn)*rand())] ;# src node
	set dest $src
	while {$dest==$src} {
		set dest [expr int($val(nn)*rand())] ;# dest node
	}

    # Traffic config
    # create agent
    set udp_src [new Agent/UDP]
    set null_sink [new Agent/Null]
    # attach to nodes
    $ns attach-agent $node($src) $udp_src
    $ns attach-agent $node($dest) $null_sink
    # connect agents
    $ns connect $udp_src $null_sink
    $udp_src set fid_ $i

    # Traffic generator
    set cbr($i) [new Application/Traffic/CBR]
    # attach to agent
    $cbr($i) attach-agent $udp_src
    $cbr($i) set packetSize_ $cbr_size
	$cbr($i) set rate_ $cbr_rate
    $cbr($i) set interval_ $cbr_interval
    $cbr($i) set random_ false 
    $cbr($i) set type_ CBR
    
    # start traffic generation
    $ns at 1.0 "$cbr($i) start"
}

#move nodes
set ms 5
set val(sn) [expr int(1 + $ms*rand())]

for {set i 0} { $i < $val(nn) } { incr i } {

#set vc 10
#for {set j 2} { $j < $vc } { incr j } {

    set x_pos_2 [expr int($rn*rand()+1)] ;#random settings
    set y_pos_2 [expr int($rn*rand()+1)] ;#random settings

    $ns at 1.0 "$node($i) setdest $x_pos_2 $y_pos_2 $val(sn)"
# }
    
}


# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 50.0001 "finish"
$ns at 50.0002 "halt_simulation"




# Run simulation
puts "Simulation starting"
$ns run

