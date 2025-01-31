package com.megait.nocoronazone.service;

import com.megait.nocoronazone.controller.AuthenticationMember;
import com.megait.nocoronazone.domain.AuthType;
import com.megait.nocoronazone.domain.Member;
import com.megait.nocoronazone.domain.MemberType;
import com.megait.nocoronazone.form.SettingForm;
import com.megait.nocoronazone.form.SignUpForm;
import com.megait.nocoronazone.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

//    @PostConstruct
//    @Profile("local")
    public void createNewMember(){

        Member member = Member.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("9sua9")
                .authType(AuthType.GENERAL)
                .certification("no")
                .build();

        memberRepository.save(member);

        member = Member.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("shienka")
                .authType(AuthType.GENERAL)
                .build();

        memberRepository.save(member);

        member = Member.builder()
                .email("qwe@qwe.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("기며녕")
                .authType(AuthType.GENERAL)
                .build();

        memberRepository.save(member);

        member = Member.builder()
                .email("qwe@qwe22.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("하팀장")
                .authType(AuthType.GENERAL)
                .build();

        memberRepository.save(member);

        member = Member.builder()
                .email("qwe@qwe52.com")
                .password(passwordEncoder.encode("qwe123"))
                .memberType(MemberType.ROLE_ADMIN)
                .nickname("완린이")
                .authType(AuthType.GENERAL)
                .build();

        memberRepository.save(member);
    }

    public Member processNewUser(SignUpForm signUpForm){

        Member member = Member.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .memberType(MemberType.ROLE_USER)
                .authType(AuthType.GENERAL)
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
        if(!member.isEmpty()){
            throw new IllegalArgumentException("nickname already exists");
        }
    }

    public void checkEmail(String email){
        Optional<Member> member = memberRepository.findByEmail(email);
        if(!member.isEmpty()){
            throw new IllegalArgumentException("email already exists");
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

        MemberUser memberUser = new MemberUser(member);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(memberUser, memberUser.getMember().getPassword(), memberUser.getAuthorities()));

    }

    public void login(Member member) {
        MemberUser memberUser = new MemberUser(member);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        memberUser,
                        memberUser.getMember().getPassword(),
                        memberUser.getAuthorities()
                );

        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(token);
    }


    @Transactional
    public Member updateMember(Long no, SettingForm settingForm, String password) {
        Member member = memberRepository.findByNo(no).get();
        member.update(settingForm);
        member.setPassword(passwordEncoder.encode(settingForm.getPassword()));

        return member;
    }


    public Object getMember(Member member) {
        return memberRepository.getById(member.getNo());
    }


    public Member getNicknameMember(String nickname) {
        Optional<Member> optionalMember = memberRepository.findByNickname(nickname);

        if(optionalMember.isEmpty()){
            throw new IllegalArgumentException("wrong nickname");
        }

        return optionalMember.get();
    }

    public void sendCodeEmailToMember(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        emailService.sendCodeEmail(optionalMember.get());
    }

    public boolean checkAuthenticationCode(String email, String code){

        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        code = code.replaceAll(" ","");

        if (optionalMember.isEmpty()){
            throw new IllegalArgumentException("wrong email");
        }

        if (optionalMember.get().getAuthenticationCode().equals(code)){
            return true;
        }

        return false;
    }

    @Transactional
    public void changePassword(String email, String password){
        Optional<Member> optionalMember = memberRepository.findByEmail(email);

        if (optionalMember.isEmpty()){
            throw new IllegalArgumentException("wrong email");
        }

        Member member = optionalMember.get();
        member.setPassword(passwordEncoder.encode(password));

    }


}



