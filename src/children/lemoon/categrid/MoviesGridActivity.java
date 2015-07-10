package children.lemoon.categrid;
 
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.devsmart.android.ui.HorizontalListView;
import com.devsmart.android.ui.HorizontalListView.OnScrollListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.AsyncHttpClient;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.MuPlayer;
import children.lemoon.myrespone.HeaderItemEntity;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponeResList;
import children.lemoon.player.org.Player;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.entry.ResponsePager;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.utils.NetworkUtils;
import children.lemoon.utils.download.DownloadManagerPro;
import children.lemoon.utils.download.PreferencesUtils;

public class MoviesGridActivity extends BaseReqActivity implements View.OnClickListener, AdapterView.OnItemClickListener  {
	private MoviesGridAdapter adapter;
	private LinkedList<PlayItemEntity> data = new LinkedList<PlayItemEntity>();
	private View goBack;
	private int mCurrentPageIndex = 1;
	private int mPageSize = 20;
	private int mTotalPageCount = 9999;
	private TextView cataName;
	private int cataTypeId;
	private MyGridView cateGrid;
	
 	public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
	//public HorizontalListView mHListView;
	//private HorizontalScrollViewAdapter mAdapter;
	
	
	private PullToRefreshScrollView prvMovieList;
	ScrollView mScrollView;
	CustomProgressDialog mLoading;
	//SharedPreferences mPrefs;
	String mCacheRoot = null;
	
	
	public static final String DOWNLOAD_FOLDER_NAME = "Story_Courseware";
    private DownloadManagerPro downloadManagerPro;
	private DownloadManager mDlMgr;
 
    private MyHandler handler;

    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;
	
	
	private boolean queryMovies() {
		if(!mLoading.isShowing())
			mLoading.show();
		HttpManger localHttpManger = new HttpManger(this, this.bHandler, this);
		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("id", String.valueOf(this.cataTypeId));
		localHashMap.put("pageindex", this.mCurrentPageIndex);
		localHashMap.put("pagesize", this.mPageSize);
		return localHttpManger.httpRequest(Configer.REQ_TYPE_CATEINFO, localHashMap, false, ResponeResList.class, false, false, true);
	}
	
	private int mLastIdx;
	private boolean mHReqing=false;
	private boolean queryHList(int lastidx, int id, int pageidx) {
		mHReqing = true;
		mLastIdx = lastidx;
		if(!mLoading.isShowing())
			mLoading.show();
		HttpManger localHttpManger = new HttpManger(this, this.bHandler, this);
		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("id", id);
		localHashMap.put("pageindex", pageidx);
		localHashMap.put("pagesize", this.mPageSize);
		return localHttpManger.httpRequest(Configer.REQ_TYPE_HLIST, localHashMap, false, ResponeResList.class, false, false, true);
	}
	
	
	public void onClick(View paramView) {
		finish();
	}


	
	
	private View getHeaderView(ViewGroup  root,boolean attachToRoot){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
	//  LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	//  LayoutInflater inflater2 = getLayoutInflater();
	    LayoutInflater inflater3 = LayoutInflater.from(this);
	    View view = inflater3.inflate(R.layout.act_cage_grid_cateheader, root,attachToRoot);
	    //view.setLayoutParams(lp);
	    return view;
	}
	
	
	
	protected void onPause() {
		super.onPause();
		mLoading.dismiss();
		unregisterReceiver(completeReceiver);  
		getContentResolver().unregisterContentObserver(downloadObserver);  

		finish();
	}
	protected void onResume() {
		super.onResume();
		downloadObserver = new DownloadChangeObserver();
		getContentResolver().registerContentObserver(  DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        
        //updateView();
	}
	
	//pcontainer;
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.act_cate_grid);

		
		mLoading =  CustomProgressDialog.createDialog(this);
		mLoading.show();
		
		this.goBack = findViewById(R.id.go_back);
		this.goBack.setOnClickListener(this);		
		
		
		handler = new MyHandler();
		mDlMgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);  
        downloadManagerPro = new DownloadManagerPro(mDlMgr);
		
		//mPrefs = PreferenceManager.getDefaultSharedPreferences(this);  
		mCacheRoot = StorageUtils.getCacheDirectory(this).getAbsolutePath();
		
		cfgImgLoader();
		this.cataName = ((TextView) findViewById(R.id.tv_movie_type));
		this.cateGrid = ((MyGridView) findViewById(R.id.gv_movies_list));
		this.prvMovieList = ((PullToRefreshScrollView) findViewById(R.id.prv_vr_movies));
		
		prvMovieList.setHeaderState(false);
		prvMovieList.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				prvMovieList.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				if (mCurrentPageIndex <= mTotalPageCount) {
					queryMovies();
					return;
				}
			}
		});

		mScrollView = prvMovieList.getRefreshableView();
		
		
		//this.prvMovieList.setOnFooterRefreshListener(this);
		
		//取参数
		String str = getIntent().getStringExtra("curCata");
		this.cataName.setText(str);
		this.cataTypeId = getIntent().getIntExtra("cataId", 0);

		
		
		cateGrid.setNumColumns(3);
		cateGrid.setHaveScrollbar(false);

		this.adapter = new MoviesGridAdapter(this, this.data, mLoader);
		this.cateGrid.setAdapter(this.adapter);
		this.cateGrid.setOnItemClickListener(this);
		queryMovies();
	}
 
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		Adapter adp1 =  paramAdapterView.getAdapter();
		PlayItemEntity pie = (PlayItemEntity)adp1.getItem(paramInt);
		
		if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}
 
//		if(paramInt == 1){
//			pie = new PlayItemEntity();
//			pie.setId(1324);
//			pie.setType(10);
//			pie.setName("test1");
//			pie.setDownUrl("http://app.znds.com/down/20150706/TJ-3.0.6-dangbei.apk");
//		}
//		else if(paramInt == 2){
//			pie = new PlayItemEntity();
//			pie.setId(5545);
//			pie.setType(10);
//			pie.setName("test2");
//			pie.setDownUrl("http://app.znds.com/down/20150602/dsm-2.5.6-dangbei.apk");			
//		}
//		else if(paramInt == 3){
//			pie = new PlayItemEntity();
//			pie.setId(33424);
//			pie.setType(10);
//			pie.setName("test3");
//			pie.setDownUrl("http://app.znds.com/down/20150608/vstqjh-2.6.7.2-dangbei.apk");			
//		} 		
		
		if(pie.getType() == 10){
			//课件
			HScrollAdapter adp = (HScrollAdapter)adp1;
			
			String idstr = String.valueOf(pie.getId());

			File f = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
			String path = f.getAbsolutePath()+"/";
			f.mkdir();
			
			//如果文件不存在 或者 id没有被保存
			if(!new File(path+idstr).exists() || PreferencesUtils.getLong(getApplicationContext(), "res_"+idstr)==-1){	
	    		if(adp.getIdx() >= 0){
	    			HorizontalListView hv = mHlistArr.get(adp.getIdx()) ;
	    			//在ListView中，使用getChildAt(index)的取值，只能是当前可见区域（列表可滚动）的子项！ 即取值范围在 >= ListView.getFirstVisiblePosition() &&  <= ListView.getLastVisiblePosition(); 
	    			Log.e("", "pos="+paramInt+", firstvispos="+hv.getFirstVisiblePosition()+", rslt="+(paramInt-hv.getFirstVisiblePosition()));
	     			View v = hv.getChildAt(paramInt-hv.getFirstVisiblePosition());
	     			View v1 = v.findViewById(R.id.dlprogress);
	     			if(v1.getTag() != null){
	     				return;
	     			}
	     			
		            //开始下载   
		            Uri resource = Uri.parse(encodeGB(pie.getDownUrl()));   
		            DownloadManager.Request request = new DownloadManager.Request(resource);   
		            request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);   
		            request.setAllowedOverRoaming(false);   
	 
		            //不在通知栏中显示   
		            request.setShowRunningNotification(false);  
		            request.setVisibleInDownloadsUi(false);  
		            
		            //sdcard的目录下的 某文件夹  
		            request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, idstr);  
		            request.setTitle(pie.getName());   
		            long downloadId  = mDlMgr.enqueue(request);  
		            v1.setTag(downloadId);
		            PreferencesUtils.putLong(getApplicationContext(), "dl_"+downloadId, adp.getIdx());
		            
	                updateView(downloadId);
	    		}				
			}
			else{
				//打开
			}
			
			
		}
		else if(pie.getType() == 0){
			Intent it = new Intent(this, MuPlayer.class);
			it.putExtra("cataId", pie.getId());
			it.putExtra("curCata",pie.getName());
			startActivity(it);			
		}
		else if(pie.getType() == 4){
			Intent it = new Intent(this, Player.class);
			it.putExtra("cataId", pie.getId());
			startActivity(it);
		}
	}

 
	
	/** 
     * 如果服务器不支持中文路径的情况下需要转换url的编码。 
     * @param string 
     * @return 
     */  
    public String encodeGB(String string)  
    {  
        //转换中文编码  
        String split[] = string.split("/");  
        for (int i = 1; i < split.length; i++) {  
            try {  
                split[i] = URLEncoder.encode(split[i], "GB2312");  
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
            split[0] = split[0]+"/"+split[i];  
        }  
        split[0] = split[0].replaceAll("\\+", "%20");//处理空格  
        return split[0];  
    }  
	

	
	LinkedList<HorizontalListView> mHlistArr = new LinkedList<HorizontalListView>();
	LinkedList<HScrollAdapter> mHAdpArr = new LinkedList<HScrollAdapter>();
	protected void onPostHandle(int paramInt1, Object paramObject1, boolean paramBoolean, int paramInt2, Object paramObject2, Object paramObject3) {
		super.onPostHandle(paramInt1, paramObject1, paramBoolean, paramInt2, paramObject2, paramObject3);
		if (paramObject1 == null) {
			mLoading.cancel();
			return;
		}

		if (paramInt1 == Configer.REQ_TYPE_CATEINFO) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) paramObject1;
			ResponeResList list = (ResponeResList) localResHeadAndBody.getBody();
 
			boolean bhasHeader = false;
			boolean bhasMain = false;
			if((list.getResList()!=null && list.getResList().size() > 0)){
				bhasMain = true;
			}
			if(list.getHeaderList()!=null && list.getHeaderList().size()>0){
				bhasHeader = true;
			}
			
			if(!bhasHeader || !bhasMain)
				return;
 
			this.data.addAll(list.getResList());
			this.adapter.notifyDataSetChanged();
 
			if(mCurrentPageIndex == 1){
				mHlistArr.clear();
				mHAdpArr.clear();
				
				LinearLayout pcontainer = (LinearLayout)MoviesGridActivity.this.findViewById(R.id.headercontainer);
				List<HeaderItemEntity> hList = list.getHeaderList();
				
				if(hList!=null && hList.size()>0){
					int i = hList.size()-1;
					for(; i>=0; i--){
						HeaderItemEntity h = hList.get(i);
						
						View v = getHeaderView(pcontainer, false);
						TextView tx = (TextView)v.findViewById(R.id.header_title);
						tx.setText(h.getTitle());
 
						
						LinkedList<PlayItemEntity> childlist = new LinkedList<PlayItemEntity>(h.getChildList());
						HScrollAdapter adp = new HScrollAdapter(MoviesGridActivity.this, childlist, mLoader);
						adp.setIdx(i);
						adp.setTypeId(h.getId());
						mHAdpArr.addFirst(adp);
						
						HorizontalListView hv = (HorizontalListView)v.findViewById(R.id.header_grid);
						hv.setTag(i); //!!!!!!!!
						hv.setAdapter(adp);
						
						hv.setOnItemClickListener(MoviesGridActivity.this);
						hv.setOnScrollListener(new OnScrollListener() {
							@Override
							public void onScrollStateChanged(View view, int scrollState) {
								// TODO Auto-generated method stub
							}
							
							@Override
							public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
								// TODO Auto-generated method stub
								Log.e("", "onScroll:  "+ firstVisibleItem+","+visibleItemCount+","+totalItemCount);

								if(view != null ){
									Log.e("", "viewtg="+(Integer)view.getTag());
									int idx = (Integer)view.getTag();
									if(idx<0 || idx >= mHAdpArr.size()){
										return;
									}
									
									HScrollAdapter adp = mHAdpArr.get(idx);
									if(!mHReqing && firstVisibleItem+visibleItemCount+5 >= totalItemCount && adp.getCount()>=mPageSize){
										int id = adp.getTypeId();
										int pgidx = adp.getPgIdx();
										queryHList(idx, id, pgidx+1);
									}
								}
							}
						});
						mHlistArr.addFirst(hv);
						pcontainer.addView(v, 0);						
							
						adp.notifyDataSetChanged();
						//v.findViewById(R.id.grid_header_title_bg).setVisibility(View.VISIBLE);
					}
 				}
//				if(bhasHeader){
//				}

				if(bhasMain){
					//lable_all
					MoviesGridActivity.this.findViewById(R.id.lable_all).setVisibility(View.VISIBLE); 
					MoviesGridActivity.this.findViewById(R.id.maingrid_header_title_bg).setVisibility(View.VISIBLE); 
					MoviesGridActivity.this.findViewById(R.id.grid_cateall_bg_bottom).setVisibility(View.VISIBLE);
				}
			}			
			
			this.mCurrentPageIndex ++;

			prvMovieList.onRefreshComplete();
			//Logger.LOGD("+++VRMoviesListActivity++" + list.getMovieList() + "++++++");
		} else if ((paramInt1 == Configer.REQ_TYPE_HLIST)) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) paramObject1;
			ResponeResList list = (ResponeResList) localResHeadAndBody.getBody();
			if(mLastIdx<0 || mLastIdx >= mHAdpArr.size()){
				mLoading.cancel();
				mHReqing = false;
				return;
			}
			
			HScrollAdapter adp = mHAdpArr.get(mLastIdx);
			adp.addData(list.getResList());
			adp.notifyDataSetChanged();
			adp.setPgIdx(adp.getPgIdx()+1);
			mHReqing = false;
		}
		mLoading.cancel();

		return;
	}
 
 //////////////////////////////////////////
    public void updateView(long downloadId) {
        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
    }
    
 
	 class DownloadChangeObserver extends ContentObserver {
	        public DownloadChangeObserver() {
	            super(handler);
	        }
	        
	        public boolean isNumeric(String str){
	        	  for (int i = str.length();--i>=0;){   
	        	   if (!Character.isDigit(str.charAt(i))){
	        	    return false;
	        	   }
	        	  }
	        	  return true;
	        }

	        @Override
	        public void onChange(boolean selfChange, Uri uri) {
	        // TODO Auto-generated method stub
	        	//super.onChange(selfChange, uri);
	        	Log.e("", "onchange: uri="+uri);
	        	String url = uri.toString();
	        	String id = url.substring(url.lastIndexOf("/")+1);
	        	if(isNumeric(id)){
	        		Log.e("", "getdlid="+id);
	        		updateView(Integer.valueOf(id));
	        	}
	        }
	 
	    }
	 
	    class CompleteReceiver extends BroadcastReceiver {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            /**
	             * get the id of download which have download success, if the id is
	             * my id and it's status is successful, then install it
	             **/
	            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//	            if (completeDownloadId == downloadId) {
//	                updateView(completeDownloadId);
//	                // if download successful  
//	                if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
//	                		
//	                }
//	            }
	        }
	    };
 
	    @SuppressLint("HandlerLeak")
	    private class MyHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);
	 
	            switch (msg.what) {
	            case 0:
	                int status = (Integer) msg.obj;
	                if (isDownloading(status)) {
//	                    downloadProgress.setVisibility(View.VISIBLE);
//	                    downloadProgress.setMax(0);
//	                    downloadProgress.setProgress(0);

	 
	                    if (msg.arg2 < 0) {
//	                        downloadProgress.setIndeterminate(true);
	                    } else {
//	                        downloadProgress.setIndeterminate(false);
//	                        downloadProgress.setMax(msg.arg2);
//	                        downloadProgress.setProgress(msg.arg1);
	                    }
	                } else {
//	                    downloadProgress.setVisibility(View.GONE);
//	                    downloadProgress.setMax(0);
//	                    downloadProgress.setProgress(0);
	 
	                    if (status == DownloadManager.STATUS_FAILED) {
	                    	
	                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
	                    	
	                    } else {
	                    	
	                    }
	                }
	                break;
	            }
	        }
	    }
	 
	    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");
	 
	    public static final int MB_2_BYTE = 1024 * 1024;
	    public static final int KB_2_BYTE = 1024;
	 
	    /**
	     * @param size
	     * @return
	     */
	    public static CharSequence getAppSize(long size) {
	        if (size <= 0) {
	            return "0M";
	        }
	 
	        if (size >= MB_2_BYTE) {
	            return new StringBuilder(16).append(
	                    DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
	                    .append("M");
	        } else if (size >= KB_2_BYTE) {
	            return new StringBuilder(16).append(
	                    DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
	                    .append("K");
	        } else {
	            return size + "B";
	        }
	    }
	 
	    public static String getNotiPercent(long progress, long max) {
	        int rate = 0;
	        if (progress <= 0 || max <= 0) {
	            rate = 0;
	        } else if (progress > max) {
	            rate = 100;
	        } else {
	            rate = (int) ((double) progress / max * 100);
	        }
	        return new StringBuilder(16).append(rate).append("%").toString();
	    }
	 
	    public static boolean isDownloading(int downloadManagerStatus) {
	        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
	                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
	                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
	    }	
	
	
	
	
	////////////////////////////////////////

	public void onVoiceCommand(int paramInt) {
	}

	ImageLoader mLoader;
	private void cfgImgLoader(){
		mLoader = ImageLoader.getInstance();
		File cacheDir = new File(mCacheRoot+"/pics/");	// ;
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
		        .memoryCacheExtraOptions(112, 130) // default = device screen dimensions
		        .diskCacheExtraOptions(112, 130, null)
		        .threadPoolSize(3) // default
		        .threadPriority(Thread.NORM_PRIORITY - 2) // default
		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        .memoryCacheSize(2 * 1024 * 1024)
		        .memoryCacheSizePercentage(13) // default
		        .diskCache(new UnlimitedDiskCache(cacheDir)) // default,  设置带时限的文件缓存是不和要求的，如果我获得不了之前文件，哪怕其过期了，我也删除不了
		        .diskCacheSize(500 * 1024 *1024)		//500M    			//所以缓存啊，还是我定期去清理
		        .diskCacheFileCount(10000)			//10000 pics    
		        .writeDebugLogs()				// Log.d()
		        .defaultDisplayImageOptions(new Builder()
		        								.cacheOnDisc(true)
		        								.cacheOnDisk(true)
		        								//.cacheInMemory(true)
		        								.showImageForEmptyUri(Configer.Res.get_icon_for_categrid())
		        								.showImageOnLoading(Configer.Res.get_icon_for_categrid())
		        								.showImageOnFail(Configer.Res.get_icon_for_categrid())
		        								.build())
		        .build();
		mLoader.init(config);
	}
}