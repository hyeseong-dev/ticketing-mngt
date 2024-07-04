//package com.mgnt.core.jwt;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class UserDetailServiceImpl implements UserDetailsService {
//
//    private final UserJpaRepository userJpaRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        return userJpaRepository.findByEmail(email)
//                .map(UserDetailsImpl::new)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//    }
//}
