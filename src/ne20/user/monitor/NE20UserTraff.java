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
public class NE20UserTraff {
    long upv4;
    long upv6;
    long downv4;
    long downv6;
    
    long millis ;
    
    public NE20UserTraff(long millis, long upv4,long upv6,long downv4,long downv6) {
        this.millis = millis;
        this.upv4 = upv4;
        this.upv6 = upv6;
        this.downv4 = downv4;
        this.downv6 = downv6;
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
