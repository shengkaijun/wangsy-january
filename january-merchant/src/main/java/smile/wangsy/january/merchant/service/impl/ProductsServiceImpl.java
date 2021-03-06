package smile.wangsy.january.merchant.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import smile.wangsy.january.merchant.mapper.ProductsMapper;
import smile.wangsy.january.merchant.model.Merchant;
import smile.wangsy.january.merchant.model.Products;
import smile.wangsy.january.merchant.service.MerchantService;
import smile.wangsy.january.merchant.service.ProductsService;
import smile.wangsy.january.merchant.dto.ProductsDto;
import smile.wangsy.january.merchant.util.CommonUtil;
import smile.wangsy.january.merchant.valid.ProductsValid;

import wang.smile.common.base.BaseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author wangsy
 * @date 2018/08/30.
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class ProductsServiceImpl extends BaseService<Products> implements ProductsService {

    @Autowired
    private MerchantService merchantService;

    @Resource
    private ProductsMapper productsMapper;

    @Override
    public void insertByDto(ProductsDto dto) throws Exception {
        Products model = ProductsDto.transfer(dto);

        Merchant currentLogin = CommonUtil.getCurrentLogin(merchantService);

        if(null != currentLogin) {
            model.setMerchantId(currentLogin.getId());
            model.setMerchantName(currentLogin.getName());
        } else {
            throw new Exception("当前账号不存在");
        }

        model.setBeenDeleted(false);
        model.setInsertTime(new Date());

        productsMapper.insert(model);
    }

    @Override
    public void updateByDto(ProductsDto dto) throws Exception {
        Products model = ProductsDto.transfer(dto);

        model.setUpdateTime(new Date());
        if(null == model.getId()) {
            throw new Exception("id不能为空");
        }

        productsMapper.updateByPrimaryKeySelective(model);
    }

    @Override
    public Products selectById(Object id) {
        Products model = productsMapper.selectByPrimaryKey(id);

        if (model!=null && model.getBeenDeleted()) {
            return null;
        }
        return model;
    }

    @Override
    public List<Products> selectByConditions(ProductsValid valid) {

        Example example = new Example(Products.class);
        Example.Criteria criteria = example.createCriteria();
        /**
         * 查询未被删除的数据
         */
        criteria.andEqualTo("beenDeleted", false);
        return productsMapper.selectByCondition(example);
    }

    @Override
    public void deleteByUpdate(Object id) {
        Products model = productsMapper.selectByPrimaryKey(id);
        model.setBeenDeleted(true);
        model.setDeleteTime(new Date());
        productsMapper.updateByPrimaryKeySelective(model);
    }

}
