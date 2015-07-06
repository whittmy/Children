package children.lemoon.categrid;
 
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
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
	
	private PullToRefreshScrollView prvMovieList;
	ScrollView mScrollView;
	CustomProgressDialog mLoading;

	
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

	public void onClick(View paramView) {
		finish();
	}

	ImageLoader mLoader;
	private void cfgImgLoader(){
		mLoader = ImageLoader.getInstance();
		File cacheDir = new File(StorageUtils.getCacheDirectory(this).getAbsoluteFile()+"/pics/");	// ;
		
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
	
	
	private View getHeaderView(){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
	//  LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	//  LayoutInflater inflater2 = getLayoutInflater();
	    LayoutInflater inflater3 = LayoutInflater.from(this);
	    View view = inflater3.inflate(R.layout.act_cage_grid_header_item, null);
	    //view.setLayoutParams(lp);
	    return view;
	}
	
	//pcontainer;
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.act_cate_grid);
		this.goBack = findViewById(R.id.go_back);
		this.goBack.setOnClickListener(this);
		
		mLoading =  CustomProgressDialog.createDialog(this);
		mLoading.show();
		
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

		
		
		cateGrid.setNumColumns(5);
		cateGrid.setHaveScrollbar(false);

		this.adapter = new MoviesGridAdapter(this, this.data, mLoader);
		this.cateGrid.setAdapter(this.adapter);
		this.cateGrid.setOnItemClickListener(this);
		queryMovies();
	}

//	public void onFooterRefresh(PullToRefreshScrollView paramPullToRefreshScrollView) {
//		if (this.mCurrentPageIndex <= this.mTotalPageCount) {
//			queryMovies();
//			return;
//		}
//		this.prvMovieList.onFooterRefreshComplete();
//	}

	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		PlayItemEntity pie = (PlayItemEntity) ((Adapter) paramAdapterView.getAdapter()).getItem(paramInt);
		if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}

		//playMovie(pie);
		
		Intent it = new Intent(this, Player.class);
		it.putExtra("cataId", pie.getId());
		startActivity(it);
	}

	protected void onPause() {
		super.onPause();
		mLoading.dismiss();
		finish();
	}

	protected void onPostHandle(int paramInt1, Object paramObject1, boolean paramBoolean, int paramInt2, Object paramObject2, Object paramObject3) {
		super.onPostHandle(paramInt1, paramObject1, paramBoolean, paramInt2, paramObject2, paramObject3);
		if (paramObject1 == null) {
			return;
		}

		if (paramInt1 == Configer.REQ_TYPE_CATEINFO) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) paramObject1;
			ResponeResList list = (ResponeResList) localResHeadAndBody.getBody();
 
			//this.mTotalPageCount = ((ResponsePager) localResHeadAndBody.getPage()).getPageCount();
			this.data.addAll(list.getResList());
			this.adapter.notifyDataSetChanged();
 
			if(mCurrentPageIndex == 1){
				LinearLayout pcontainer = (LinearLayout)MoviesGridActivity.this.findViewById(R.id.headercontainer);
				List<HeaderItemEntity> hList = list.getHeaderList();
 
				if(hList!=null && hList.size()>0){
					int i = hList.size()-1;
					for(; i>=0; i--){
						HeaderItemEntity h = hList.get(i);
						
						View v = getHeaderView();
						TextView tx = (TextView)v.findViewById(R.id.header_title);
						tx.setText(h.getTitle());
						
						GridView gv = (GridView)v.findViewById(R.id.header_grid);
						int childcnt = h.getChildList().size();
 
						////////////////////hscrollview内嵌单行gridview关键之处 ////////////////////
						/// 计算gridview完整宽度，以及单个item的宽度，以dp为单位
						///我们设定单个item的宽度为149像素，
				        int length = 149;	
				        DisplayMetrics dm = new DisplayMetrics();
				        getWindowManager().getDefaultDisplay().getMetrics(dm);
				        float density = dm.density;
				        int gridviewWidth = (int) (childcnt * (length + 10) * density); //这里的10为 padding的值
				        int itemWidth = (int) (length * density);

				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				                gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
				        gv.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
				        gv.setColumnWidth(itemWidth); // 设置列表项宽
				        gv.setNumColumns(childcnt); // 设置列数量=列表集合数
						////////////////////////////////////
 
						LinkedList<PlayItemEntity> childlist = new LinkedList<PlayItemEntity>();
						childlist.addAll(h.getChildList());
						MoviesGridAdapter adp = new MoviesGridAdapter(MoviesGridActivity.this, childlist, mLoader);
						gv.setAdapter(adp);
						gv.setOnItemClickListener(MoviesGridActivity.this);						
						pcontainer.addView(v, 0);						

						adp.notifyDataSetChanged();
					}
					
					//lable_all
					MoviesGridActivity.this.findViewById(R.id.lable_all).setVisibility(View.VISIBLE); 
 				}
			}			
			
			this.mCurrentPageIndex ++;

			prvMovieList.onRefreshComplete();
			//Logger.LOGD("+++VRMoviesListActivity++" + list.getMovieList() + "++++++");
		} else if ((paramInt1 == 22)) {
			// VirPlayer.CreatePlayer(this,
			// ((ResponseMovieDetailInfo)((ResHeadAndBody)paramObject1).getBody()).getMovieDetailInfo()).start();
		}
		mLoading.cancel();

		return;
	}
 
	
	
	
	protected void onResume() {
		super.onResume();
	}

	public void onVoiceCommand(int paramInt) {
	}

 
}
