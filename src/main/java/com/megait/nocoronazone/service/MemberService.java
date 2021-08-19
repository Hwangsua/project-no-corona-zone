package com.megait.nocoronazone.service;

import com.megait.nocoronazone.domain.Member;
import com.megait.nocoronazone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.
                of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Member member = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(member));
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getMemberType().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());

    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(entity -> {

                    entity.setNickname(attributes.getNickname());
                    entity.setEmail(attributes.getEmail());
                    return entity;
                })
                .orElse(attributes.toEntity());

        return memberRepository.save(member);

    }


import com.megait.nocoronazone.domain.MemberType;
import com.megait.nocoronazone.form.SignUpForm;
import com.megait.nocoronazone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Profile("local")
    public void createNewMember(){

        Member member = Member.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("하하")
                .build();

        Member newMember = memberRepository.save(member);

    }

    public Member processNewUser(SignUpForm signUpForm){

        Member member = Member.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .memberType(MemberType.ROLE_USER)
                .build();

        Member newMember = memberRepository.save(member);

        emailService.sendEmail(newMember);

        return newMember;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optional = memberRepository.findByEmail(username);
        if (optional.isEmpty()){
            throw new UsernameNotFoundException(username);
        }

        return new MemberUser(optional.get());
    }


    public void checkNickname(String nickname) {

        Optional<Member> member = memberRepository.findByNickname(nickname);
        if(member.isEmpty()){
            throw new IllegalArgumentException("available nickname");
        }
    }

    @Transactional
    public void checkEmailToken(String token, String email) {

        Optional<Member> opt = memberRepository.findByEmail(email);

        if (opt.isEmpty()){
            throw new IllegalArgumentException("wrong email");
        }

        Member member = opt.get();
        if(!member.isValidToken(token)){
            throw new IllegalArgumentException("wrong token");
        }

        member.completeSignup();
    }
}
