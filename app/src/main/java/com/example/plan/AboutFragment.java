package com.example.plan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    public static final String filename = "aboutFragmentUser";
    private static final int RC_CHOOSE_PHOTO = 2;
    private static final String KEY_NAME = "name";
    private static final String KEY_TEXT = "text";
    private static final String KEY_ICON = "icon";

    private TextView tvSetting, tvAppreciate, tvAbout, tvUserName, tvUserText, tvMail;
    private ImageView ivUserIcon;

    private String userNameStr, userTextStr, filePath;

    private Map<String, Object> saveMap = new HashMap<String, Object>();

    public AboutFragment() {
        // Required empty public constructor
    }


    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        tvAbout = (TextView)view.findViewById(R.id.fragment_about_item_about);
        tvAppreciate = (TextView)view.findViewById(R.id.fragment_about_item_appreciate);
        tvUserName = (TextView)view.findViewById(R.id.about_user_tv_name);
        tvUserText = (TextView)view.findViewById(R.id.about_user_tv_text);
        tvSetting = (TextView)view.findViewById(R.id.fragment_about_item_setting);
        tvMail = (TextView)view.findViewById(R.id.fragment_about_item_mail);

        ivUserIcon = (ImageView)view.findViewById(R.id.about_user_ic_icon);

        ivUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //申请权限，
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CHOOSE_PHOTO);
                }
                else
                {
                    //选择照片
                    choosePhoto();
                }
            }
        });

        tvUserText.setOnClickListener(userOnClickListener);
        tvUserName.setOnClickListener(userOnClickListener);

        tvAppreciate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RewardActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        tvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        tvMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MailActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });


        //读取用户信息，
        try{
            FileInputStream f_in_finish = getActivity().openFileInput(filename);
            ObjectInputStream ois_finish = new ObjectInputStream(f_in_finish);
            saveMap = extracted(ois_finish);
            f_in_finish.close();

            userNameStr = (String) saveMap.get(KEY_NAME);
            userTextStr = (String) saveMap.get(KEY_TEXT);
            filePath = (String)saveMap.get(KEY_ICON);

            if(userNameStr != null && !(userNameStr.equals("")))
                tvUserName.setText(userNameStr);
            if(userTextStr != null && !(userTextStr.equals("")))
                tvUserText.setText(userTextStr);
            if(filePath != null && !(filePath.equals("")))
            {
                Uri uri = Uri.parse(filePath);
                ivUserIcon.setImageURI(uri);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }



        return view;
    }

    private Map<String, Object> extracted(ObjectInputStream ois_finish) throws IOException, ClassNotFoundException {
        return (Map<String, Object>)ois_finish.readObject();
    }

    private View.OnClickListener userOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPopupWindow();
        }
    };

    //对话框显示
    private void showPopupWindow()
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.popup_set_user, null);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x30ffffff));
        popupWindow.setAnimationStyle(R.style.Animation_Dialog);
        popupWindow.setFocusable(true);

        final EditText name = (EditText)view.findViewById(R.id.popup_ed_user_name);
        final EditText text = (EditText)view.findViewById(R.id.popup_ed_user_text);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId())
                {
                    case R.id.popup_btn_cancel:
                        popupWindow.dismiss();
                        break;
                    case R.id.popup_btn_ok:
                        String nameStr = name.getText().toString();
                        String textStr = text.getText().toString();
                        if(!nameStr.equals(""))
                        {
                            userNameStr = nameStr;
                            tvUserName.setText(userNameStr);
                        }
                        if(!textStr.equals(""))
                        {
                            userTextStr = textStr;
                            tvUserText.setText(userTextStr);
                        }
                        popupWindow.dismiss();
                        break;
                }
            }
        };
        view.findViewById(R.id.popup_btn_cancel).setOnClickListener(clickListener);
        view.findViewById(R.id.popup_btn_ok).setOnClickListener(clickListener);
        popupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER_VERTICAL, 0, 0);
    }



    //选择照片
    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
    }
    //接收返回来的数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //返回值为-1时为选择了照片， 0为未选择
        if(resultCode == -1) {
            switch (requestCode) {
                case RC_CHOOSE_PHOTO:
                    Uri uri = data.getData();
                    ivUserIcon.setImageURI(uri);
                    filePath = FileUtil.getFilePathByUri(getContext(), uri);
                    break;
            }
        }
    }

    //写入用户数据
    @Override
    public void onStop()
    {
        saveMap.clear();
        if(userNameStr != null && !(userNameStr.equals("")))
        {
            saveMap.put(KEY_NAME, userNameStr);
        }
        if(userTextStr != null && !(userTextStr.equals("")))
        {
            saveMap.put(KEY_TEXT, userTextStr);
        }
        if(filePath != null && !(filePath.equals("")))
        {
            saveMap.put(KEY_ICON, filePath);
        }


        try {
            FileOutputStream f_out_todo = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos_todo = new ObjectOutputStream(f_out_todo);
            oos_todo.writeObject(saveMap);
            f_out_todo.flush();
            f_out_todo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

}
