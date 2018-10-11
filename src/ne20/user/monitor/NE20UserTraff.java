/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import org.snmp4j.PDU;
import org.snmp4j.smi.OctetString;

/**
 *
 * @author edsongley
 */

public class NE20UserTraff {
    long upv4;
    long upv6;
    long downv4;
    long downv6;
    String qosProfile;
    String mac;
    String ipv4;
    OctetString startDate;
    String ipv6_wanaddr;
    String ipv6_wanprefix;
    String ipv6_lanprefix;
    int vlan;
    
    long millis;
    
    public NE20UserTraff(PDU response) {
        this.millis = java.lang.System.currentTimeMillis();
        this.upv4 = response.get(0).getVariable().toLong();
        this.upv6 = response.get(1).getVariable().toLong();
        this.downv4 = response.get(2).getVariable().toLong();
        this.downv6 = response.get(3).getVariable().toLong();
        this.qosProfile = response.get(4).getVariable().toString();
        this.mac = response.get(5).getVariable().toString();
        this.ipv4 = response.get(6).getVariable().toString();
        this.startDate = (OctetString)response.get(7).getVariable();
        this.ipv6_wanaddr = response.get(8).getVariable().toString();
        this.ipv6_wanprefix = response.get(9).getVariable().toString();
        this.ipv6_lanprefix = response.get(10).getVariable().toString();
        this.vlan = response.get(11).getVariable().toInt();
    }

    public String getQosProfile() {
        return qosProfile;
    }
    
    public NE20UserTraff(
            long millis, long upv4,long upv6,
            long downv4,long downv6,String qosProfile,
            String mac,String ipv4,String ipv6_wanaddr,
            String ipv6_wanprefix,String ipv6_lanprefix,int vlan) {
        this.millis = millis;
        this.upv4 = upv4;
        this.upv6 = upv6;
        this.downv4 = downv4;
        this.downv6 = downv6;
        this.qosProfile = qosProfile;
        this.mac = mac;
        this.ipv4 = ipv4;
        this.ipv6_wanaddr = ipv6_wanaddr;
        this.ipv6_wanprefix = ipv6_wanprefix;
        this.ipv6_lanprefix = ipv6_lanprefix;
        this.vlan = vlan;
    }

    public String getMac() {
        return mac;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6_wanaddr() {
        return ipv6_wanaddr.equals("00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00") ? "::/0" : ipv6_wanaddr.replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2");
    }

    public String getIpv6_wanprefix() {
        return ipv6_wanprefix.equals("00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00") ? "::/0" : ipv6_wanprefix.replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2");
    }

    public String getIpv6_lanprefix() {
        return ipv6_lanprefix.equals("00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00") ? "::/0" : ipv6_lanprefix.replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2");
    }

    public int getVlan() {
        return vlan;
    }
    
    public long getUploadBits(){
        return (upv4+upv6) * 8;
    }
    public long getUploadBytes(){
        return (upv4+upv6);
    }
    public long getDownloadBits(){
        return (downv4+downv6) * 8;
    }
    public long getDownloadBytes(){
        return (downv4+downv6);
    }
    
    public long getMillis(){
        return millis;
    }
}
