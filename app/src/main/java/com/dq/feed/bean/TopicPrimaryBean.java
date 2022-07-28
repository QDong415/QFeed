package com.dq.feed.bean;

import java.io.Serializable;
import java.util.List;

import static com.dq.feed.tool.QUtilKt.getTimeStringFromNow;

public class TopicPrimaryBean implements Serializable {

	private String tid;
	private String userid;
	private String name;//发布人姓名
	private String avatar;//发布人头像model
	private int gender;//发布人
	private int age;//发布人
	private String content;//内容
	private List<AvatarBean> pictures;//9张图
	private int create_time;//发布时间

	private boolean like;//我是否点过赞
	private int likecount;
	private int commentcount;
	private int sharecount;
	private List<CommentBean> comments;//最多3条评论，可能为nil

	private String videourl;//视频路径

	private String cityname;
	private String adname;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getCreate_time() {
		return create_time;
	}

	public void setCreate_time(int create_time) {
		this.create_time = create_time;
	}

	public int getCommentcount() {
		return commentcount;
	}

	public void setCommentcount(int commentcount) {
		this.commentcount = commentcount;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	// 我这里只是为了测试@功能和表情功能，你们记得要删掉 +" [微笑]" + "@" + hashCode()
	public String getContent() {
		return content  +" [微笑]" + "@" + hashCode();
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isLike() {
		return like;
	}

	public void setLike(boolean like) {
		this.like = like;
	}

	public int getLikecount() {
		return likecount;
	}

	public void setLikecount(int likecount) {
		this.likecount = likecount;
	}

	public List<AvatarBean> getPictures() {
		return pictures;
	}

	public void setPictures(List<AvatarBean> pictures) {
		this.pictures = pictures;
	}

	public List<CommentBean> getComments() {
		return comments;
	}

	public void setComments(List<CommentBean> comments) {
		this.comments = comments;
	}

	public String getVideourl() {
		return videourl;
	}

	public void setVideourl(String videourl) {
		this.videourl = videourl;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getSharecount() {
		return sharecount;
	}

	public void setSharecount(int sharecount) {
		this.sharecount = sharecount;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getAdname() {
		return adname;
	}

	public void setAdname(String adname) {
		this.adname = adname;
	}

}
