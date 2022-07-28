package com.dq.feed.bean;

import java.io.Serializable;
import java.util.List;

public class CommentPrimaryBean implements Serializable {

	private int cid; //评论的id
	private String userid;
	private String name;
	private String avatar;

	private String to_userid;
	private String to_name;
	private String to_avatar;

	private String content;
	private int create_time;

	private List<CommentPrimaryBean> items;//最多3条评论，可能为nil
	private List<String> pictures;//9张图

	private int pcid;//父评论（为0表示是大评论）
	private boolean like;//1我赞过，0未赞
	private int likecount;
	private int childcount;//有几条子评论

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
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getCreate_time() {
		return create_time;
	}

	public void setCreate_time(int create_time) {
		this.create_time = create_time;
	}

	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getTo_name() {
		return to_name;
	}
	public void setTo_name(String to_name) {
		this.to_name = to_name;
	}
	public String getTo_userid() {
		return to_userid;
	}
	public void setTo_userid(String to_userid) {
		this.to_userid = to_userid;
	}
	public String getTo_avatar() {
		return to_avatar;
	}
	public void setTo_avatar(String to_avatar) {
		this.to_avatar = to_avatar;
	}

	public List<CommentPrimaryBean> getItems() {
		return items;
	}

	public void setItems(List<CommentPrimaryBean> items) {
		this.items = items;
	}

	public List<String> getPictures() {
		return pictures;
	}

	public void setPictures(List<String> pictures) {
		this.pictures = pictures;
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

	public int getChildcount() {
		return childcount;
	}

	public void setChildcount(int childcount) {
		this.childcount = childcount;
	}

	public int getPcid() {
		return pcid;
	}

	public void setPcid(int pcid) {
		this.pcid = pcid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
}

