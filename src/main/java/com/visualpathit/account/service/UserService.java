package com.visualpathit.account.service;

import java.util.List;

import com.visualpathit.account.model.User;
import org.springframework.data.domain.Page;

/** {@author imrant}!*/
public interface UserService {
	/** {@inheritDoc}} !*/
    void save(User user);
    /** {@inheritDoc}} !*/
    User findByUsername(String username);
    User findById(long id);
    /*public void updateUser(User user);*/
    public List <User> getList();
    /** One page of users ordered by id; page is zero-based. */
    Page<User> getPage(int page, int size);
}
