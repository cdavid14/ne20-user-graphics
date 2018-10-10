/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

/**
 *
 * @author edsongley
 */
public class NE20Info {
    public NE20Info(){
        this.ip = "255.255.255.255";
        this.snmpv2comm="public";
    }
    public NE20Info(String ipv4, String snmp){
        this.ip = ipv4;
        this.snmpv2comm = snmp;
    }
    public String ip = "255.255.255.255";
    public String snmpv2comm="public";
    
    public String toString(){
        return this.ip;
    }
    
}