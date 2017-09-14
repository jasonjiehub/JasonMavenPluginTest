package org.apache.maven.plugins.dao;

import java.util.List;
import org.apache.maven.plugins.entity.TbClassify;

public interface TbClassifyMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TbClassify record);

    TbClassify selectByPrimaryKey(Integer id);

    List<TbClassify> selectAll();

    int updateByPrimaryKey(TbClassify record);
}