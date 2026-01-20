package com.damda.domain.auth.service;

import com.damda.domain.auth.model.AuthReq;
import com.damda.domain.auth.model.AuthRes;
import com.damda.domain.auth.model.AuthSignupReq;

public interface SocialService {
    AuthRes login(AuthReq dto, String socialToken);
    AuthRes signup(AuthSignupReq dto, String socialToken);
}