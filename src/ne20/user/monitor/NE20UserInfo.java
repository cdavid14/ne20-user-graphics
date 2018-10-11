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
public class NE20UserInfo {
    public NE20UserInfo(String login,int ne20id){
        this.login = login;
        this.ne20id = ne20id;
    }
    public NE20UserInfo(){ }
    public String login = "";
    public int ne20id = -1;
    
}
