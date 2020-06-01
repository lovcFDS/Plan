package com.example.plan;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlanListFragment extends Fragment {

    //map key定义
    public static final String filename = "plan_datas";
    public static final String KEY_TODO = "todo";
    public static final String KEY_TIME = "time";
    public static final String KEY_ISEND = "isEnd";

    //组件定义
    private EditText ed_todo;
    private ImageView img_title;
    private TextView tv_title;
    private TextView tv_fromWho;
    private static TextView tv_nums;
    private ListView listViewTodo;
    private Button btn_add;
    private static Button btn_allEndOrBegin;
    private Button btn_clear;
    private ProgressDialog progressDialog;
    private static boolean flag_all_finish = false;    //控制allEndOfBegin按钮的点击事件

    private Context context;

    //数据源
    private List<Map<String, Object>> todoListItems;
    private FragmentListAdapter adapterTodo;

    //抬头语定义 和 抬头语索引定义，偷个懒 不请求网络信息，焊死车门
    String titles[] = new String[]{
            "恨不得挂长绳于青天，糸此西飞之白日。",
            "最严重的浪费就是时间的浪费。",
            "时间待人是同等的，而时间在每个人手里的价值却不同。",
            "今天应做的事没有做，明天再早也是耽误了。",
            "时间就象海绵里的水一样，只要你愿挤，总还是有的。",
            "时间是伟大的导师。",
            "普通人只想到如何度过时间，有才能的人设法利用时间。",
            "利用时间是一个极其高级的规律。"
    };
    String names[] = new String[]{
            "——李白",
            "——布封",
            "——佚名 ",
            "——裴斯泰洛齐",
            "——鲁迅",
            "——伯克",
            "——叔本华 ",
            "——恩格斯"
    };
    private Random random = new Random();       //用以随机生成抬头语
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    public PlanListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plan_list, container, false);

        img_title = (ImageView)view.findViewById(R.id.imageView);
        tv_title = (TextView)view.findViewById(R.id.main_tv_title);
        tv_nums = (TextView)view.findViewById(R.id.main_tv_nums);
        tv_fromWho = (TextView)view.findViewById(R.id.main_tv_fromwho);
        listViewTodo = (ListView)view.findViewById(R.id.main_list_todo);
        btn_add = (Button)view.findViewById(R.id.main_btn_add);
        btn_allEndOrBegin = (Button)view.findViewById(R.id.main_btn_allendofdone);
        btn_clear = (Button)view.findViewById(R.id.main_btn_clear);
        ed_todo = (EditText)view.findViewById(R.id.main_edtv_todo);

        //头部的组件使用同一个点击事件
        img_title.setOnClickListener(titleOnClickListener);
        tv_title.setOnClickListener(titleOnClickListener);
        tv_fromWho.setOnClickListener(titleOnClickListener);

        //数据源初始化
        todoListItems = new ArrayList<Map<String, Object>>();

        //使用线程 去读取本地文件
        progressDialog = ProgressDialog.show(getContext(), "计划清单", "请稍等...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //finishList读取
                //todoList读取
                try{
                    FileInputStream f_in_finish = getActivity().openFileInput(filename);
                    ObjectInputStream ois_finish = new ObjectInputStream(f_in_finish);
                    todoListItems = extracted(ois_finish);
                    f_in_finish.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                adapterTodo.notifyDataSetChanged();
                updateNum();
                //发送空消息
                handler.sendEmptyMessage(0);

            }
        }).start();

        adapterTodo = new FragmentListAdapter(getActivity());

        //设置ListView的数据更新器
        listViewTodo.setAdapter(adapterTodo);

        //添加按钮事件
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todo_info = ed_todo.getText().toString();
                if(todo_info.equals(""))
                {
                    Toast.makeText(getContext(), "你的计划不能为空！", Toast.LENGTH_SHORT).show();
                }else
                {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(KEY_ISEND, false);
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    String dateString = ""+ year + "." + (month+1) + "." + day;
                    map.put(KEY_TIME, dateString);
                    map.put(KEY_TODO, todo_info);
                    todoListItems.add(map);
                    adapterTodo.notifyDataSetChanged();
                    ed_todo.setText("");
                }
                //更新计划数量
                updateNum();
            }
        });
        //清除全部的计划
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加对话框，避免误删全部计划
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("提示");
                builder.setMessage("将清除全部的计划，确认操作？");
                builder.setIcon(R.drawable.my_ic_lanuncher);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        todoListItems.clear();
                        adapterTodo.notifyDataSetChanged();
                        updateNum();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });
        //遍历数据源 将所有事件设置为完成或者未完成。
        //更新：将全部完成与全部未完成集合在一个按钮上，通过flag_all_finish控制，该变量会在updateNums内发生改变
        //用以确定执行全部完成或者全部未完成
        btn_allEndOrBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag_all_finish)
                {
                    for(int i = 0; i < todoListItems.size(); i++)
                    {
                        todoListItems.get(i).put(KEY_ISEND, false);
                    }
                    adapterTodo.notifyDataSetChanged();

                }
                else
                {
                    for(int i = 0; i < todoListItems.size(); i++)
                    {
                        todoListItems.get(i).put(KEY_ISEND, true);
                    }
                    adapterTodo.notifyDataSetChanged();
                }

                updateNum();
            }
        });

        randomTitle();
        updateNum();
        return view;
    }


    /* 用以在其他操作过后 更新显示计划数的TextView */
    public void updateNum()
    {
        int max_nums = todoListItems.size();
        int end_count = 0;
        for(int i = 0; i < max_nums; i++)
        {
            if((boolean)todoListItems.get(i).get(KEY_ISEND))
            {
                end_count++;
            }
        }
        if(max_nums == end_count)
        {
            tv_nums.setText(max_nums + "个计划全部完成。");
            flag_all_finish = true;
            btn_allEndOrBegin.setText("全部未完成");
        }
        else {
            tv_nums.setText(max_nums + "个计划，" + end_count + "个已完成。");
            flag_all_finish = false;
            btn_allEndOrBegin.setText("全部完成");
        }
    }

    @Override
    public void onStop() {
        //将todo数据存入内部存储
        progressDialog = ProgressDialog.show(getContext(), "计划清单", "请稍等...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //写入todoList到文件
                //写入finishList到文件
                try {
                    FileOutputStream f_out_todo = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                    ObjectOutputStream oos_todo = new ObjectOutputStream(f_out_todo);
                    oos_todo.writeObject(todoListItems);
                    f_out_todo.flush();
                    f_out_todo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //发送空消息
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
        while(thread.isAlive());
        super.onStop();
    }


    //从文件读取List  数据源
    private List<Map<String, Object>> extracted(ObjectInputStream ois) throws OptionalDataException, ClassCastException, IOException, ClassNotFoundException {
        return (List<Map<String, Object>>)ois.readObject();
    }

    //头部点击事件
    public View.OnClickListener titleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            randomTitle();
        }
    };
    //随机显示一句抬头语和来自谁说得
    public void randomTitle()
    {
        int i = random.nextInt(titles.length);
        tv_title.setText(titles[i]);
        tv_fromWho.setText(names[i]);
    }

    private class FragmentListAdapter extends BaseAdapter {

        //map key定义
        public static final String filename = "plan_datas";
        public static final String KEY_TODO = "todo";
        public static final String KEY_TIME = "time";
        public static final String KEY_ISEND = "isEnd";

        private LayoutInflater layoutInflater;
        private Context context;

        public FragmentListAdapter(Context context)
        {
            this.context = context;

            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return todoListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return todoListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null)
            {
                view = layoutInflater.inflate(R.layout.fragment_plan_list_item, null);
            }else
            {
                view = convertView;
            }
            CheckBox cb = view.findViewById(R.id.list_item_check);
            final TextView tv_time = view.findViewById(R.id.list_item_time);
            final TextView tv_todo = view.findViewById(R.id.list_item_todo);
            Button btn_delete = view.findViewById(R.id.list_item_delete);

            //勾选框事件绑定，对数据源和信息进行操作
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        //设置文字颜色和划线效果
                        tv_todo.setTextColor(0xff999999);
                        tv_todo.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                        todoListItems.get(position).put(KEY_ISEND, true);
                    }
                    else
                    {
                        //取消划线效果和恢复颜色
                        tv_todo.setTextColor(tv_time.getTextColors());
                        tv_todo.getPaint().setFlags(0);
                        todoListItems.get(position).put(KEY_ISEND, false);
                    }
                    updateNum();
                }
            });
            //清除Item事件绑定
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    todoListItems.remove(position);
                    notifyDataSetChanged();

                }
            });

            //设置要显示的值
            cb.setChecked(todoListItems.get(position).get(KEY_ISEND).equals(true));
            tv_time.setText(todoListItems.get(position).get(KEY_TIME).toString());
            tv_todo.setText(todoListItems.get(position).get(KEY_TODO).toString());

            return view;
        }
    }
}
