package com.jeff.main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHbyJSch{
	static Logger logger = Logger.getLogger(SSHbyJSch.class);
	static Session session;
	static final String src = "/Users/jeff/Desktop/";
	static final String dest = "/home/jeff/Desktop/";
	static final String fileName = "xxx.pdf";//abc.txt
	static final String user="";
	static final String host="172.20.10.3";//192.168.1.1
	static final String passwd = "";
	static final int port=22;
	
	public static void main(String[] args) throws Exception {
		
		//step 1 create connect
	    initSSH(user, host, passwd, port);
	    //step 2 put file via SSH
		putFile(src, dest, fileName);
	    //step 3 execute remote command 
		execCommand(". ~/Desktop/tmp.bat");
		session.disconnect();
		logger.info("session disconnect");
	}
	static void initSSH(String user,String host, String passwd, int port){
		try {
			JSch jsch=new JSch();
			session=jsch.getSession(user, host, port);
			UserInfo ui = new MyUserInfo(){
				public void showMessage(String message){
//		          JOptionPane.showMessageDialog(null, message);
				}
				public boolean promptYesNo(String message){
//		          Object[] options={ "yes", "no" };
//		          int foo=JOptionPane.showOptionDialog(null, 
//		                                               message,
//		                                               "Warning", 
//		                                               JOptionPane.DEFAULT_OPTION, 
//		                                               JOptionPane.WARNING_MESSAGE,
//		                                               null, options, options[0]);
					return true;
				}
			};
			
			session.setPassword(passwd);
			session.setUserInfo(ui);
			
			session.connect(9000);
			logger.info(String.format("session isConnected:%s, host:%s", session.isConnected(), session.getHost()));
		} catch (Exception e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}
	static void putFile(String src, String dest, String fileName) throws Exception{
		try {
			logger.info(String.format("do putFile(%s, %s, %s)",src, dest, fileName));
			ChannelSftp channelSftp =(ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.cd(dest);
		    InputStream is = new FileInputStream(src.concat(fileName)); 
		    logger.info("size of source file >> "+is.available());
	        channelSftp.put(new BufferedInputStream(is), "ext.pdf", ChannelSftp.OVERWRITE);
		    logger.info("putFile channelSftp.pwd() >>"+channelSftp.pwd());
		    //compare size between source file and destination file 
		    logger.info("size of dest file >> " + IOUtils.toByteArray(channelSftp.get(dest.concat(fileName))).length);
	        channelSftp.exit();
		} catch (JSchException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}
	static void execCommand(String command){
		try {
			  logger.info(String.format("execCommand(%s)",command));
			  ChannelExec channel=(ChannelExec)session.openChannel("exec");
			  channel.setCommand(command);
		      channel.setInputStream(null);
		      channel.setErrStream(System.err);
		      InputStream ins = new BufferedInputStream(channel.getInputStream());
		      channel.connect();
		      
		      List<String> lines = IOUtils.readLines(ins);
		      
		      for(String line: lines){
		    	  System.out.println(line);
		      }
		      //Official site offer example
//		      byte[] tmp=new byte[1024];
//		      while(true){
//		        while(ins.available()>0){
//		          int i=ins.read(tmp, 0, 1024);
//		          if(i<0)break;
//		          logger.info(new String(tmp, 0, i));
//		        }
//		        if(channel.isClosed()){
//		          if(ins.available()>0) continue; 
//		          logger.info("exit-status: "+channel.getExitStatus());
//		          break;
//		        }
//		        try{
//		        	Thread.sleep(1000);
//		        }catch(Exception ee){
//		        	ee.printStackTrace();
//		        }
//		      }
		      channel.disconnect();
		} catch (Exception e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}
}
