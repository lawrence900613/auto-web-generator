package com.example.autowebgenerator.mapper;

import com.example.autowebgenerator.model.entity.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis Flex mapper for the User entity.
 * Inherits full CRUD + pagination from BaseMapper.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
