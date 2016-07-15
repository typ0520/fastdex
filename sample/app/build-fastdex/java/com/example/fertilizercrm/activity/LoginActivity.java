package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import com.example.fertilizercrm.common.utils.IntentUtils;
import com.example.fertilizercrm.common.utils.JsonUtils;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.utils.SharedPreferenceUtil;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.easemob.EMCallBack;
import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.example.fertilizercrm.easemob.chatuidemo.Constant;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.easemob.chatuidemo.db.UserDao;
import com.example.fertilizercrm.easemob.chatuidemo.domain.User;
import com.example.fertilizercrm.BuildConfig;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.LoginResponse;
import com.example.fertilizercrm.http.BizError;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.role.Role;
import org.apache.http.Header;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity {
    /**
     * 当登陆成功后跳转到主页面
     */
    public static final int FLAG_TO_MAIN_PAGE = 128 << 1;

    /**
     * 按下返回键回到桌面
     */
    public static final int FLAG_TO_LAUNCH_WHEN_BACK = 128 << 2;

    public static final String KEY_USERNAME = "KEY_USERNAME";
    public static final String KEY_PWD = "KEY_PWD";

    @Bind(R.id.et_user) EditText et_user;
    @Bind(R.id.et_pwd)  EditText et_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        String username = SharedPreferenceUtil.getStringValueFromSP(getPackageName(), KEY_USERNAME);
        String pwd = SharedPreferenceUtil.getStringValueFromSP(getPackageName(), KEY_PWD);
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            et_user.setText(username);
            et_pwd.setText(pwd);
            Selection.setSelection(et_user.getText(), et_user.getText().length() - 1);
        }

        if (BuildConfig.DEBUG) {
            findViewById(R.id.btn_test).setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_test) void testAccountLogin() {
        final List<Map<String,String>> list = new ArrayList<>();
        list.add(new HashMap<String, String>(){{
            put(KEY_USERNAME,"testcj");
            put(KEY_PWD,"111111");
        }});
        list.add(new HashMap<String, String>(){{
            put(KEY_USERNAME,"testcjyw");
            put(KEY_PWD,"111111");
        }});
        list.add(new HashMap<String, String>(){{
            put(KEY_USERNAME,"testyj");
            put(KEY_PWD,"111111");
        }});
        list.add(new HashMap<String, String>() {{
            put(KEY_USERNAME, "testyjyw");
            put(KEY_PWD, "111111");
        }});
        list.add(new HashMap<String, String>() {{
            put(KEY_USERNAME, "testej");
            put(KEY_PWD, "111111");
        }});
        list.add(new HashMap<String, String>() {{
            put(KEY_USERNAME, "testejyw");
            put(KEY_PWD, "111111");
        }});
        final String[] strings = new String[]{"厂家", "厂家业务员","批发商","批发商业务员","零售商","零售商业务员"};
        new AlertView("请选择角色", null, null, null, strings, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                String uname = list.get(position).get(KEY_USERNAME);
                String pwd = list.get(position).get(KEY_PWD);

                et_user.setText(uname);
                et_pwd.setText(pwd);
                login();
            }
        }).show();
    }

    @OnClick(R.id.btn_submit) void login() {
//        if (true) {
//            startActivity(AreaActivity.class);
//            return;
//        }
        final String username = et_user.getText().toString();
        final String password = et_pwd.getText().toString();
        if (TextUtils.isEmpty(username)) {
            showLongToast("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showLongToast("请输入密码");
            return;
        }

        showProgress();
        new Req().url(URLText.login)
                .addParam("username", username)
                .addParam("password", password)
                .get(new Callback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                        LoginResponse response = JsonUtils.jsonToObject(obj.toString(),LoginResponse.class);
                        response.setUserinfo(obj.optJSONObject("message"));
                        Logger.d("login response: " + response.toString());

                        // 登陆成功，保存用户名密码
                        SharedPreferenceUtil.setStringDataIntoSP(getPackageName(), KEY_USERNAME, username);
                        SharedPreferenceUtil.setStringDataIntoSP(getPackageName(), KEY_PWD, password);

                        Role.setCurrentRole(Role.valueOf(Integer.valueOf(response.getMUerType())));
                        DataManager.getInstance().setLoginResponse(response);
                        try {
                            String username = response.getToken() + "_" + response.getMUerType();
                            String pwd = response.getHpassword();
                            if (TextUtils.isEmpty(pwd)) {
                                pwd = "111111";
                            }
                            Logger.d("easemob: user: " + username);

//                            hideProgress();
//                            if ((getIntent().getFlags() & FLAG_TO_MAIN_PAGE) != 0) {
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent);
//                            }
//                            finish();

//                            if (BuildConfig.BUILD_TYPE.equals("debug")) {
//                                emLogin("test", pwd);
//                            } else {
//                                emLogin(username, pwd);
//                            }
                            emLogin(username, pwd);
                        } catch (IllegalArgumentException e) {
                            showLongToast("IM用户名或密码错误");
                        }
                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, BizError error) {
                        hideProgress();
                        showLongToast(error.getMessage());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideProgress();
                        showLongToast("网络异常");
                    }
                });
    }

    @Override
    protected boolean onBack() {
        if ((getIntent().getFlags() & FLAG_TO_LAUNCH_WHEN_BACK) != 0) {
            //回到桌面
            IntentUtils.backToLauncher(this);
            return true;
        }
        return super.onBack();
    }

    private void emLogin(final String username, final String pwd) {
        EMChatManager.getInstance().login(username, pwd, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Logger.d("main", "登陆聊天服务器成功！");

                        FertilizerApplication.getInstance().setUserName(username);
                        FertilizerApplication.getInstance().setPassword(pwd);

                        try {
                            // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                            // ** manually load all local groups and
                            EMGroupManager.getInstance().loadAllGroups();
                            EMChatManager.getInstance().loadAllConversations();
                            // 处理好友和群组
                            initializeContacts();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 取好友或者群聊失败，不让进入主页面
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    hideProgress();
                                    DemoHXSDKHelper.getInstance().logout(true, null);
                                    Toast.makeText(getApplicationContext(), com.example.fertilizercrm.R.string.login_failure_failed, Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                        boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                                FertilizerApplication.currentUserNick.trim());
                        if (!updatenick) {
                            Log.e("LoginActivity", "update current user nick fail");
                        }

                        final String jpushTag = DataManager.getInstance().getLoginResponse().getToken();
                        Logger.d(">> jpushTag: " + jpushTag);
                        try {
                            JPushInterface.setAliasAndTags(getActivity(), jpushTag, new HashSet<String>() {{
                                add(jpushTag);
                            }}, new TagAliasCallback() {
                                @Override
                                public void gotResult(int i, String s, Set<String> set) {
                                    if (i == 0) {
                                        Logger.d("<<<<jpuash setAliasAndTags 别名设置成功: " + jpushTag);
                                    } else {
                                        Logger.e("<<<<jpuash setAliasAndTags 别名设置失败");
                                    }
                                }
                            });
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        hideProgress();
                        if ((getIntent().getFlags() & FLAG_TO_MAIN_PAGE) != 0) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        showLongToast("登陆聊天服务器失败!");
                    }
                });
                Logger.e("main", "登陆聊天服务器失败!");
                Logger.d("user: " + username + ",pwd: " + pwd);
            }
        });
    }

    private void initializeContacts() {
        Map<String, User> userlist = new HashMap<String, User>();
        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(
                com.example.fertilizercrm.R.string.Application_and_notify);
        newFriends.setNick(strChat);

        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        User groupUser = new User();
        String strGroup = getResources().getString(com.example.fertilizercrm.R.string.group_chat);
        groupUser.setUsername(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        userlist.put(Constant.GROUP_USERNAME, groupUser);
//
//        // 添加"Robot"
//        User robotUser = new User();
//        String strRobot = getResources().getString(com.example.fertilizercrm.R.string.robot_chat);
//        robotUser.setUsername(Constant.CHAT_ROBOT);
//        robotUser.setNick(strRobot);
//        robotUser.setHeader("");
//        userlist.put(Constant.CHAT_ROBOT, robotUser);

        // 存入内存
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
        // 存入db
        UserDao dao = new UserDao(LoginActivity.this);
        List<User> users = new ArrayList<User>(userlist.values());
        dao.saveContactList(users);
    }
}
