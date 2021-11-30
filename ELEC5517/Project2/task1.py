#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

TOPOS = {'mytopo' : (lambda : myNetwork())}

def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                         #controller=RemoteController,
			 #ip="172.17.0.5",
			 controller=Controller,
                         protocol='tcp',
                         port=6633)

    info( '*** Add switches\n')
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch)
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch)
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch)
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch)

    info( '*** Add hosts\n')
    h8 = net.addHost('h8', cls=Host, ip='10.0.0.8', defaultRoute=None)
    h5 = net.addHost('h5', cls=Host, ip='10.0.0.5', defaultRoute=None)
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', defaultRoute=None)
    h4 = net.addHost('h4', cls=Host, ip='10.0.0.4', defaultRoute=None)
    h6 = net.addHost('h6', cls=Host, ip='10.0.0.6', defaultRoute=None)
    h3 = net.addHost('h3', cls=Host, ip='10.0.0.3', defaultRoute=None)
    h9 = net.addHost('h9', cls=Host, ip='10.0.0.9', defaultRoute=None)
    h7 = net.addHost('h7', cls=Host, ip='10.0.0.7', defaultRoute=None)
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', defaultRoute=None)
   
    info( '*** Add links\n')
    net.addLink(s1, s2)
    net.addLink(s2, h1, cls=TCLink, bw=1000, delay='5ms')
    net.addLink(s2, h2, cls=TCLink, bw=1000, delay='5ms')
    net.addLink(s2, h3, cls=TCLink, bw=1000, delay='5ms')
    net.addLink(s1, s3)
    net.addLink(s3, h4, cls=TCLink, bw=1, delay='5ms')
    net.addLink(s3, h5, cls=TCLink, bw=1, delay='5ms')
    net.addLink(s3, h6, cls=TCLink, bw=1, delay='5ms')
    net.addLink(s1, s4)
    net.addLink(s4, h7, cls=TCLink, bw=100, delay='5ms')
    net.addLink(s4, h8, cls=TCLink, bw=100, delay='5ms')
    net.addLink(s4, h9, cls=TCLink, bw=100, delay='5ms')


    intf = h1.defaultIntf()
    h1.cmd('ifconfig %s inet 0' % intf)
    h1.cmd('vconfig add %s %d' % (intf, 100))
    h1.cmd('ifconfig %s.%d inet 10.0.0.1' % (intf, 100))
    newName = '%s.%d' % (intf, 100)
    intf.name = newName
    h1.nameToIntf[newName] = intf

    intf = h2.defaultIntf()
    h2.cmd('ifconfig %s inet 0' % intf)
    h2.cmd('vconfig add %s %d' % (intf, 100))
    h2.cmd('ifconfig %s.%d inet 10.0.0.2' % (intf, 100))
    newName = '%s.%d' % (intf, 100)
    intf.name = newName
    h2.nameToIntf[newName] = intf

    intf = h3.defaultIntf()
    h3.cmd('ifconfig %s inet 0' % intf)
    h3.cmd('vconfig add %s %d' % (intf, 100))
    h3.cmd('ifconfig %s.%d inet 10.0.0.3' % (intf, 100))
    newName = '%s.%d' % (intf, 100)
    intf.name = newName
    h3.nameToIntf[newName] = intf

    intf = h4.defaultIntf()
    h4.cmd('ifconfig %s inet 0' % intf)
    h4.cmd('vconfig add %s %d' % (intf, 200))
    h4.cmd('ifconfig %s.%d inet 10.0.0.4' % (intf, 200))
    newName = '%s.%d' % (intf, 200)
    intf.name = newName
    h4.nameToIntf[newName] = intf

    intf = h5.defaultIntf()
    h5.cmd('ifconfig %s inet 0' % intf)
    h5.cmd('vconfig add %s %d' % (intf, 200))
    h5.cmd('ifconfig %s.%d inet 10.0.0.5' % (intf, 200))
    newName = '%s.%d' % (intf, 200)
    intf.name = newName
    h5.nameToIntf[newName] = intf

    intf = h6.defaultIntf()
    h6.cmd('ifconfig %s inet 0' % intf)
    h6.cmd('vconfig add %s %d' % (intf, 200))
    h6.cmd('ifconfig %s.%d inet 10.0.0.6' % (intf, 200))
    newName = '%s.%d' % (intf, 200)
    intf.name = newName
    h6.nameToIntf[newName] = intf

    intf = h7.defaultIntf()
    h7.cmd('ifconfig %s inet 0' % intf)
    h7.cmd('vconfig add %s %d' % (intf, 300))
    h7.cmd('ifconfig %s.%d inet 10.0.0.7' % (intf, 300))
    newName = '%s.%d' % (intf, 300)
    intf.name = newName
    h7.nameToIntf[newName] = intf

    intf = h8.defaultIntf()
    h8.cmd('ifconfig %s inet 0' % intf)
    h8.cmd('vconfig add %s %d' % (intf, 300))
    h8.cmd('ifconfig %s.%d inet 10.0.0.8' % (intf, 300))
    newName = '%s.%d' % (intf, 300)
    intf.name = newName
    h8.nameToIntf[newName] = intf

    intf = h9.defaultIntf()
    h9.cmd('ifconfig %s inet 0' % intf)
    h9.cmd('vconfig add %s %d' % (intf, 300))
    h9.cmd('ifconfig %s.%d inet 10.0.0.9' % (intf, 300))
    newName = '%s.%d' % (intf, 300)
    intf.name = newName
    h9.nameToIntf[newName] = intf

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s3').start([c0])
    net.get('s2').start([c0])
    net.get('s4').start([c0])
    net.get('s1').start([c0])

    info( '*** Post configure switches and hosts\n')

    CLI(net)
    #c1 = net.addController('c1', controller=RemoteController, ip="172.17.0.5")
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()

