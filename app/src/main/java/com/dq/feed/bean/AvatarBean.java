package com.dq.feed.bean;

import android.content.Context;

import java.io.Serializable;

import static com.dq.feed.tool.QUtilKt.BASE_URL;
import static com.dq.feed.tool.QUtilKt.QINIU_URL;
import static com.dq.feed.tool.UIExtendKt.dp2px;

import com.dq.feed.R;

public class AvatarBean implements Serializable{

	private String filename; //新版本图片返回的是json
	private int width;
	private int height;

	//client,如果用get开头，会导致fastjson把这个方法也存进去
	public String findOriginalUrl() {
		if (filename!=null) {
			//说明是新版本
			if (filename.startsWith("http")) {
				//服务器返回的直接就是地址，不需要手机端再拼接头部
				return filename;
			}else{
				//服务器返回的文件名，需要手机端再拼接头部
				return QINIU_URL + filename ;
			}
		}
		return "";
	}

	//client ,如果用get开头，会导致fastjson把这个方法也存进去
	public String findSmallUrl(Context context, boolean isSinglePhoto) {
		if (filename == null) {
			return "";
		}

		if (!isSinglePhoto){
			//说明是>1张图
			if (filename.startsWith("http")) {
				//服务器返回的直接就是地址，不需要手机端再拼接头部，只拼接缩略图后缀
				return filename + (filename.endsWith(".gif")?"":"?imageView2/3/w/360/h/360");
			} else {
				//服务器返回的文件名，需要手机端再拼接头部，再拼接缩略图后缀
				return QINIU_URL + filename + (filename.endsWith(".gif")?"":"?imageView2/3/w/360/h/360");
			}
		}

		//说明是1张图
		int topic_griditem_maxwidth = (int) dp2px(context, 240);
		int topic_griditem_minwidth = (int) dp2px(context, 56);
		int qiniuParamsWidth = 0;
		int qiniuParamsHeight = 0;
		if (height < width) {
			// 图片很扁
			// 用这句代码，如果图片很小（比MAX框还小），则拉伸模糊到MAX框的宽度
			qiniuParamsWidth = topic_griditem_maxwidth;
			qiniuParamsHeight = (height * qiniuParamsWidth) / width;
		} else {
			// 图片很高
			qiniuParamsHeight = topic_griditem_maxwidth;
			qiniuParamsWidth = (width * qiniuParamsHeight) / height;

			if (qiniuParamsWidth < topic_griditem_minwidth) {
				qiniuParamsWidth = topic_griditem_minwidth;
			}
		}

		if (filename.startsWith("http")) {
			//服务器返回的直接就是地址，不需要手机端再拼接头部，只拼接缩略图后缀
			return filename + (filename.endsWith(".gif")?"":"?imageView2/1/w/"+qiniuParamsWidth+"/h/"+qiniuParamsHeight);
		} else {
			//服务器返回的文件名，需要手机端再拼接头部，再拼接缩略图后缀
			return QINIU_URL + filename + (filename.endsWith(".gif")?"":"?imageView2/1/w/"+qiniuParamsWidth+"/h/"+qiniuParamsHeight);
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isEmpty(){
		if(filename == null || filename.equals("")){
			return true;
		}else{
			return false;
		}
	}
}
