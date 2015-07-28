 #!/usr/bin/python

import MySQLdb
import os
import sys
import logging
import time

logger = logging.getLogger('monit_log')
logger.setLevel(logging.DEBUG)
hdr = logging.StreamHandler()
hdr.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
hdr.setFormatter(formatter)
logger.addHandler(hdr)

def monitorator_command():
    global conn,cur,name
    conn=MySQLdb.connect(host='202.114.10.148 ',port=3306,user='root',passwd='KFniuchao88',db='androidlxc')
    cur=conn.cursor()

    while True:
        cur.execute("select `name`,`status` from `lxc` where `status` in (5,2,1,0) group by `status` desc ")
        lxcdata=cur.fetchall()

        if not lxcdata[0][1]==5:
            logger.debug('No container is perparing.')
            if not lxcdata[0][1]==2:
                logger.debug('No container is free.')
                if lxcdata[0][1]==1:
                    name = lxcdata[0][0]
                    logger.debug('Starting a paused container.')
                    resume()
                elif lxcdata[0][1]==0:
                    name = lxcdata[0][0]
                    logger.debug('Starting a stopped container.')
                    start()
                else:
                    logger.debug('Creating a new container.')
                    create()
                conn.commit()
        time.sleep(30)

    cur.close()
    conn.close()

def create():
    import random
    import string

    global name
    name=''.join(random.sample(string.ascii_letters+string.digits,15))
    if(os.system("python newlxc.py -n "+name)==0):
        time = datetime.datetime.today()
        sql="INSERT INTO `lxc`(`name`,`status`,`mtime`,`ctime`) VALUES(%s,%s,%s,%s)"
        cur.execute(sql,(name,'5',time,time))
        start()
        logger.debug('Create a new container successfully')

def start():
    cur.execute("SELECT * FROM `ip` WHERE `inuse` = 0 LIMIT 1")
    ipdata = cur.fetchall()
    ipnew = ipdata[0][1]
    ipid = '%d'%ipdata[0][0]
    cur.execute("UPDATE `ip` SET `inuse` = '1' WHERE `id` = "+ipid+"")
    os.system("lxc-start -n "+name+" -s lxc.network.ipv4="+ipnew+"/24")
    cur.execute("UPDATE `lxc` SET `status` = '2',`ip`='"+ipnew+"' WHERE `name`='"+name+"'")
    logger.debug('Start a container successfully')
 
def resume():
     if(os.system("lxc-unfreeze -n "+name)==0):
         cur.execute("UPDATE `lxc` SET `status` = '2' WHERE `name`='"+name+"'")
         logger.debug('Start a paused container successfully')

def main():
    monitorator_command()
 
if __name__ == "__main__":
     main()


