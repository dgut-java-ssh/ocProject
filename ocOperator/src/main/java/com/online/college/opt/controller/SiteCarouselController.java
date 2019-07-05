package com.online.college.opt.controller;

import java.io.File;
import java.io.IOException;

import com.online.college.common.util.IDUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.online.college.common.page.TailPage;
import com.online.college.common.storage.QiniuStorage;
import com.online.college.common.storage.ThumbModel;
import com.online.college.common.web.JsonView;
import com.online.college.core.consts.domain.ConstsSiteCarousel;
import com.online.college.core.consts.service.IConstsSiteCarouselService;

import javax.servlet.http.HttpServletRequest;

/**
 * 轮播配置
 */
@Controller
@RequestMapping("/carousel")
public class SiteCarouselController{
	
	@Autowired
	private IConstsSiteCarouselService entityService;

	/**
	 * 分页展示轮播图片信息
	 * @param queryEntity
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/queryPage")
	public  ModelAndView queryPage(ConstsSiteCarousel queryEntity , TailPage<ConstsSiteCarousel> page){
		ModelAndView mv = new ModelAndView("cms/carousel/pagelist");
		mv.addObject("curNav", "carousel");
		page.setPageSize(5);//每页5条数据
		page = entityService.queryPage(queryEntity,page);
		mv.addObject("page",page);
		mv.addObject("queryEntity",queryEntity);
		return mv;
	}

	/**
	 * 根据id查询修改的轮播图片并跳转到（修改/添加）界面 merge.html
	 */
	@RequestMapping(value = "/toMerge")
	public ModelAndView toMerge(ConstsSiteCarousel entity){
		ModelAndView mv = new ModelAndView("cms/carousel/merge");
		mv.addObject("curNav", "carousel");
		
		if(entity.getId() != null){
			entity = entityService.getById(entity.getId());
//			if(null != entity && StringUtils.isNotEmpty(entity.getPicture())){
//				String pictureUrl = QiniuStorage.getUrl(entity.getPicture(),ThumbModel.THUMB_128);
//				entity.setPicture(pictureUrl);
//			}
		}
		mv.addObject("entity",entity);
		return mv;
	}

	/**
	 * 保存或添加图片
	 * @param entity
	 * @param pictureImg
	 * @return
	 */
	@RequestMapping(value = "/doMerge")
	public ModelAndView doMerge(ConstsSiteCarousel entity, @RequestParam MultipartFile pictureImg, HttpServletRequest request){
		try {
			if (null != pictureImg && pictureImg.getBytes().length > 0) {
				String path = null;// 文件路径
				String fileName = pictureImg.getOriginalFilename();// 文件原名称
				//System.out.println("上传的文件原名称:" + fileName);
				// 项目在容器中实际发布运行的根路径
				String realPath = request.getSession().getServletContext().getRealPath("/")+"static\\upload\\";
				// 自定义的文件名称
				// 扩展名
				String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
				IDUtils idUtils = new IDUtils();
				String trueFileName = idUtils.genImageName() + "." + extName;
				// 设置存放图片文件的路径
				path = realPath + trueFileName;
				//System.out.println("存放图片文件的路径:" + path);
				// 转存文件到指定的路径
				pictureImg.transferTo(new File(path));

				// 响应上传图片的url
				String url = "/static/upload/" + trueFileName;
				//System.out.println("文件成功上传到指定目录下:" + url);

				//key = QiniuStorage.uploadImage(pictureImg.getBytes());
				entity.setPicture(url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(entity.getId() == null){
			entityService.createSelectivity(entity);
		}else{
			entityService.updateSelectivity(entity);
		}
		return new ModelAndView("redirect:/carousel/queryPage.html");
	}

	/**
	 * 根据id删除图片
	 * @param entity
	 * @return
	 */
	@RequestMapping(value = "/delete")
	@ResponseBody
	public String delete(ConstsSiteCarousel entity){
		entityService.delete(entity);
		return new JsonView().toString();
	}
	
}

