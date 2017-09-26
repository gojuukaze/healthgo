package cn.ikaze.healthgo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.ikaze.healthgo.model.StepModel;
import cn.ikaze.healthgo.model.StepTransaction;
import cn.ikaze.healthgo.step.StepService;
import io.realm.Realm;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * Created by gojuukaze on 16/8/17.
 * Email: i@ikaze.uu.me
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private TextView showSteps;
    private View mLayout;
    Switch on_off, foreground_model;
    SharedPreferences sharedPreferences;
    EventBus bus;
    long numSteps;
    boolean isServiceRun;
    boolean isforeground_model;
    TextView btn;
    TextView about;
    LineChartView lineChart;
    List<PointValue> mPointValues = new ArrayList<>();
    List<AxisValue> mAxisXValues = new ArrayList<>();

    public void mybt(View v) {
        showPopupWindow(v);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
//        params.setMargins(0,getStatusBarHeight(), 0, 0);
//        toolbar.setLayoutParams(params);
        setSupportActionBar(toolbar);
        btn = (TextView) findViewById(R.id.bt);
        Typeface Font = Typeface.createFromAsset(this.getAssets(), "iconfont.ttf");
        btn.setText(getResources().getText(R.string.setting));
        btn.setTypeface(Font);

        Log.d("eee", "on create()");
        showSteps = (TextView) findViewById(R.id.showSteps);
        mLayout = findViewById(R.id.mylayout);
        on_off = (Switch) findViewById(R.id.on_off);
        foreground_model = (Switch) findViewById(R.id.foreground_model);


        sharedPreferences = getSharedPreferences("conf", MODE_PRIVATE);

        detectService();

        bus = EventBus.getDefault();
        bus.register(this);

        Realm realm = Realm.getDefaultInstance();
        StepModel result = realm.where(StepModel.class)
                .equalTo("date", DateTimeHelper.getToday())
                .findFirst();
        numSteps = result == null ? 0 : result.getNumSteps();
        bus.post(true);
        updateShowSteps();
        realm.close();

        drawChart();


    }

    public void drawChart() {

        // WeatherChartView mCharView = (WeatherChartView) findViewById(R.id.line_char);
        Date[] days = DateTimeHelper.get6days();

        Realm realm = Realm.getDefaultInstance();

        int[] data = new int[]{0, 0, 0, 0, 0, 0};
        int i = 0;
        for (Date d : days) {
            Log.d("eee","date "+d);
            if (i == 5) {
                data[i] = Integer.parseInt(String.valueOf(numSteps));
            }
            else {
                StepModel result = realm.where(StepModel.class)
                        .equalTo("date", d)
                        .findFirst();
                if (result != null) {
                    Log.d("eee","r !null  ");
                    data[i] = Integer.parseInt(String.valueOf(result.getNumSteps()));
                }
            }
            i++;
        }

        realm.close();

        String[] xValues = DateTimeHelper.get6days(true);


        lineChart = (LineChartView) findViewById(R.id.line_chart);
        for (i = 0; i < xValues.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(xValues[i]));
        }

        for (i = 0; i < data.length; i++) {
            mPointValues.add(new PointValue(i, data[i]));
        }
        initLineChart();//初始化

    }

    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFFAFA"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.WHITE);  //设置字体颜色
        //axisX.setName("date");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();

        axisY.setName("");//y轴标注
        // axisY.setTextSize(10);//设置字体大小
        axisY.setTextColor(Color.parseColor("#ffffff"));
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
//        Viewport v = new Viewport(lineChart.getMaximumViewport());
//        v.left = 0;
//        v.right= 7;
//        lineChart.setCurrentViewport(v);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateSteps(Long num) {
        numSteps = num;
        updateShowSteps();
    }

    public void updateShowSteps() {
        String text = "" + numSteps;

        if (numSteps >= 10000000)
            showSteps.setTextSize(45);

        else if (numSteps >= 1000000)
            showSteps.setTextSize(50);
        else if (numSteps >= 100000)
            showSteps.setTextSize(55);
        else if (numSteps >= 10000) {
            notifyIsUpToStandard( "太棒了，你今天超过1万步了");
            showSteps.setTextSize(60);
        }

        else {
            showSteps.setTextSize(66);
            if (numSteps>=5000) notifyIsUpToStandard("加油，你已经再走走你就达到1万步了");
            else notifyIsUpToStandard("你今天都没怎么走路，快出门运动吧");
        }
        showSteps.setText(text);

    }

    private void notifyIsUpToStandard(String msg)
    {
        MyApplication app = (MyApplication) getApplication();
        if(!app.isShowToast()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            app.setShowToast(true);
        }

    }

    private void showPopupWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        MyApplication app = (MyApplication) getApplication();
        isServiceRun=app.getServiceRun();

        isforeground_model=sharedPreferences.getBoolean("foreground_model",false);

        View contentView = LayoutInflater.from(this).inflate(
                R.layout.setting_layout, null);
        PopupWindow popupWindow = new PopupWindow(contentView,
                370, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        on_off = (Switch) contentView.findViewById(R.id.on_off);
        foreground_model = (Switch) contentView.findViewById(R.id.foreground_model);
        on_off.setChecked(isServiceRun);
        foreground_model.setChecked(isforeground_model);

        on_off.setOnCheckedChangeListener(this);
        foreground_model.setOnCheckedChangeListener(this);
        about = (TextView) contentView.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAbout();

            }
        });


//        popupWindow.setTouchable(true);
//
//        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                Log.d("mengdd", "onTouch : ");
//
//                return false;
//                // 这里如果返回true的话，touch事件将被拦截
//                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
//            }
//        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(
//                R.drawable.selectmenu_bg_downward));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    public void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("确定", null);
        builder.setCancelable(true);
        View mview = LayoutInflater.from(this).inflate(R.layout.about_me, null);
        TextView t = (TextView) mview.findViewById(R.id.version_name);
        String s = getVersionName(this);
        if (t != null) {
            t.setText("v" + s);
        }

        builder.setView(mview);
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("eee", "activity stop()");
        bus.post(false);
        if (bus.isRegistered(this))
            bus.unregister(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == R.id.on_off) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("switch_on", isChecked);
            editor.apply();
            Intent intent = new Intent(this, StepService.class);

            if (isChecked) {
                intent.putExtra("isActivity", true);
                if (!bus.isRegistered(this))
                    bus.register(this);
                startService(intent);
                bus.post(true);
            } else {
                editor.putBoolean("foreground_model", isChecked);
                editor.apply();
                foreground_model.setChecked(false);
                if (bus.isRegistered(this))
                    bus.unregister(this);
                stopService(intent);
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new StepTransaction(DateTimeHelper.getToday(), numSteps));
                realm.close();
            }
        } else if (buttonView.getId() == R.id.foreground_model) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("foreground_model", isChecked);
            editor.apply();

            Intent intent = new Intent(this, StepService.class);
            if (isChecked) {
                editor.putBoolean("switch_on", isChecked);
                editor.apply();
                on_off.setChecked(true);
                intent.putExtra("foreground_model", "on");
                intent.putExtra("isActivity", true);
                if (!bus.isRegistered(this))
                    bus.register(this);
                bus.post(true);
            } else {
                intent.putExtra("foreground_model", "off");
            }
            startService(intent);
        }

    }

    public void detectService() {
        MyApplication app = (MyApplication) getApplication();
        isServiceRun = app.getServiceRun();
        boolean temp = sharedPreferences.getBoolean("switch_on", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isServiceRun != temp) {
            if (!isServiceRun) {
                Toast.makeText(getApplicationContext(), "计步服务意外终止,请把应用加入白名单",
                        Toast.LENGTH_LONG).show();
            }
            editor.putBoolean("switch_on", isServiceRun);
            editor.apply();
        }

        temp = sharedPreferences.getBoolean("foreground_model", false);
        if (temp && !isServiceRun) {
            editor.putBoolean("foreground_model", false);
            editor.apply();
            isforeground_model = false;
        } else isforeground_model = temp;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void sdWrite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(mLayout, "申请权限",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            0);
                                }
                            })
                            .show();
                } else {
                    this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }


            }
        }
    }

}
