package com.example.fertilizercrm.easemob.chatuidemo.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.fertilizercrm.easemob.chatuidemo.Constant;
import com.example.fertilizercrm.easemob.chatuidemo.domain.InviteMessage;
import com.example.fertilizercrm.easemob.chatuidemo.domain.RobotUser;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.easemob.util.HanziToPinyin;

public class DemoDBManager {
    static private DemoDBManager dbMgr = new DemoDBManager();
    private DbOpenHelper dbHelper;
    
    void onInit(Context context){
        dbHelper = DbOpenHelper.getInstance(context);
    }
    
    public static synchronized DemoDBManager getInstance(){
        return dbMgr;
    }
    
    /**
     * 保存好友list
     * 
     * @param contactList
     */
    synchronized public void saveContactList(List<User> contactList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(UserDao.TABLE_NAME, null, null);
            for (User user : contactList) {
                ContentValues values = new ContentValues();
                values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
                if(user.getNick() != null)
                    values.put(UserDao.COLUMN_NAME_NICK, user.getNick());
                if(user.getAvatar() != null)
                    values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
                db.replace(UserDao.TABLE_NAME, null, values);
            }
        }
    }

    /**
     * 获取好友list
     * 
     * @return
     */
    synchronized public Map<String, User> getContactList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, User> users = new HashMap<String, User>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + UserDao.TABLE_NAME /* + " desc" */, null);
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_ID));
                String nick = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_NICK));
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_AVATAR));
                User user = new User();
                user.setUsername(username);
                user.setNick(nick);
                user.setAvatar(avatar);
                String headerName = null;
                if (!TextUtils.isEmpty(user.getNick())) {
                    headerName = user.getNick();
                } else {
                    headerName = user.getUsername();
                }
                
                if (username.equals(Constant.NEW_FRIENDS_USERNAME) || username.equals(Constant.GROUP_USERNAME)
                        || username.equals(Constant.CHAT_ROOM)|| username.equals(Constant.CHAT_ROBOT)) {
                    user.setHeader("");
                } else if (Character.isDigit(headerName.charAt(0))) {
                    user.setHeader("#");
                } else {
                    user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1))
                            .get(0).target.substring(0, 1).toUpperCase());
                    char header = user.getHeader().toLowerCase().charAt(0);
                    if (header < 'a' || header > 'z') {
                        user.setHeader("#");
                    }
                }
                users.put(username, user);
            }
            cursor.close();
        }
        return users;
    }
    
    /**
     * 删除一个联系人
     * @param username
     */
    synchronized public void deleteContact(String username){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(UserDao.TABLE_NAME, UserDao.COLUMN_NAME_ID + " = ?", new String[]{username});
        }
    }
    
    /**
     * 保存一个联系人
     * @param user
     */
    synchronized public void saveContact(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
        if(user.getNick() != null)
            values.put(UserDao.COLUMN_NAME_NICK, user.getNick());
        if(user.getAvatar() != null)
            values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
        if(db.isOpen()){
            db.replace(UserDao.TABLE_NAME, null, values);
        }
    }
    
    public void setDisabledGroups(List<String> groups){
        setList(UserDao.COLUMN_NAME_DISABLED_GROUPS, groups);
    }
    
    public List<String>  getDisabledGroups(){       
        return getList(UserDao.COLUMN_NAME_DISABLED_GROUPS);
    }
    
    public void setDisabledIds(List<String> ids){
        setList(UserDao.COLUMN_NAME_DISABLED_IDS, ids);
    }
    
    public List<String> getDisabledIds(){
        return getList(UserDao.COLUMN_NAME_DISABLED_IDS);
    }
    
    synchronized private void setList(String column, List<String> strList){
        StringBuilder strBuilder = new StringBuilder();
        
        for(String hxid:strList){
            strBuilder.append(hxid).append("$");
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(column, strBuilder.toString());

            db.update(UserDao.PREF_TABLE_NAME, values, null,null);
        }
    }
    
    synchronized private List<String> getList(String column){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + column + " from " + UserDao.PREF_TABLE_NAME,null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String strVal = cursor.getString(0);
        if (strVal == null || strVal.equals("")) {
            return null;
        }
        
        cursor.close();
        
        String[] array = strVal.split("$");
        
        if(array != null && array.length > 0){
            List<String> list = new ArrayList<String>();
            for(String str:array){
                list.add(str);
            }
            
            return list;
        }
        
        return null;
    }
    
    /**
     * 保存message
     * @param message
     * @return  返回这条messaged在db中的id
     */
    public synchronized Integer saveMessage(InviteMessage message){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int id = -1;
        if(db.isOpen()){
            ContentValues values = new ContentValues();
            values.put(InviteMessgeDao.COLUMN_NAME_FROM, message.getFrom());
            values.put(InviteMessgeDao.COLUMN_NAME_GROUP_ID, message.getGroupId());
            values.put(InviteMessgeDao.COLUMN_NAME_GROUP_Name, message.getGroupName());
            values.put(InviteMessgeDao.COLUMN_NAME_REASON, message.getReason());
            values.put(InviteMessgeDao.COLUMN_NAME_TIME, message.getTime());
            values.put(InviteMessgeDao.COLUMN_NAME_STATUS, message.getStatus().ordinal());
            db.insert(InviteMessgeDao.TABLE_NAME, null, values);
            
            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + InviteMessgeDao.TABLE_NAME,null); 
            if(cursor.moveToFirst()){
                id = cursor.getInt(0);
            }
            
            cursor.close();
        }
        return id;
    }
    
    /**
     * 更新message
     * @param msgId
     * @param values
     */
    synchronized public void updateMessage(int msgId,ContentValues values){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.update(InviteMessgeDao.TABLE_NAME, values, InviteMessgeDao.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(msgId)});
        }
    }
    
    /**
     * 获取messges
     * @return
     */
    synchronized public List<InviteMessage> getMessagesList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<InviteMessage> msgs = new ArrayList<InviteMessage>();
        if(db.isOpen()){
            Cursor cursor = db.rawQuery("select * from " + InviteMessgeDao.TABLE_NAME + " desc",null);
            while(cursor.moveToNext()){
                InviteMessage msg = new InviteMessage();
                int id = cursor.getInt(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_ID));
                String from = cursor.getString(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_FROM));
                String groupid = cursor.getString(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_GROUP_ID));
                String groupname = cursor.getString(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_GROUP_Name));
                String reason = cursor.getString(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_REASON));
                long time = cursor.getLong(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_TIME));
                int status = cursor.getInt(cursor.getColumnIndex(InviteMessgeDao.COLUMN_NAME_STATUS));
                
                msg.setId(id);
                msg.setFrom(from);
                msg.setGroupId(groupid);
                msg.setGroupName(groupname);
                msg.setReason(reason);
                msg.setTime(time);
                if(status == InviteMessage.InviteMesageStatus.BEINVITEED.ordinal())
                    msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
                else if(status == InviteMessage.InviteMesageStatus.BEAGREED.ordinal())
                    msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
                else if(status == InviteMessage.InviteMesageStatus.BEREFUSED.ordinal())
                    msg.setStatus(InviteMessage.InviteMesageStatus.BEREFUSED);
                else if(status == InviteMessage.InviteMesageStatus.AGREED.ordinal())
                    msg.setStatus(InviteMessage.InviteMesageStatus.AGREED);
                else if(status == InviteMessage.InviteMesageStatus.REFUSED.ordinal())
                    msg.setStatus(InviteMessage.InviteMesageStatus.REFUSED);
                else if(status == InviteMessage.InviteMesageStatus.BEAPPLYED.ordinal()){
                    msg.setStatus(InviteMessage.InviteMesageStatus.BEAPPLYED);
                }
                msgs.add(msg);
            }
            cursor.close();
        }
        return msgs;
    }
    
    synchronized public void deleteMessage(String from){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(InviteMessgeDao.TABLE_NAME, InviteMessgeDao.COLUMN_NAME_FROM + " = ?", new String[]{from});
        }
    }
    
    synchronized public void closeDB(){
        if(dbHelper != null){
            dbHelper.closeDB();
        }
    }
    
    
    /**
     * Save Robot list
     */
	synchronized public void saveRobotList(List<RobotUser> robotList) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(UserDao.ROBOT_TABLE_NAME, null, null);
			for (RobotUser item : robotList) {
				ContentValues values = new ContentValues();
				values.put(UserDao.ROBOT_COLUMN_NAME_ID, item.getUsername());
				if (item.getNick() != null)
					values.put(UserDao.ROBOT_COLUMN_NAME_NICK, item.getNick());
				if (item.getAvatar() != null)
					values.put(UserDao.ROBOT_COLUMN_NAME_AVATAR, item.getAvatar());
				db.replace(UserDao.ROBOT_TABLE_NAME, null, values);
			}
		}
	}
    
    /**
     * load robot list
     */
	synchronized public Map<String, RobotUser> getRobotList() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Map<String, RobotUser> users = null;
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + UserDao.ROBOT_TABLE_NAME, null);
			if(cursor.getCount()>0){
				users = new HashMap<String, RobotUser>();
			};
			while (cursor.moveToNext()) {
				String username = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_ID));
				String nick = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_NICK));
				String avatar = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_AVATAR));
				RobotUser user = new RobotUser();
				user.setUsername(username);
				user.setNick(nick);
				user.setAvatar(avatar);
				String headerName = null;
				if (!TextUtils.isEmpty(user.getNick())) {
					headerName = user.getNick();
				} else {
					headerName = user.getUsername();
				}
				if(Character.isDigit(headerName.charAt(0))){
					user.setHeader("#");
				}else{
					user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target
							.substring(0, 1).toUpperCase());
					char header = user.getHeader().toLowerCase().charAt(0);
					if (header < 'a' || header > 'z') {
						user.setHeader("#");
					}
				}
				
				users.put(username, user);
			}
			cursor.close();
		}
		return users;
	}
    
    
    
}