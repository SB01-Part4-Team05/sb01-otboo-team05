package com.part4.team05.sb01otbooteam05.domain.recommend.controller;

import com.part4.team05.sb01otbooteam05.domain.auth.security.CustomUserDetails;
import com.part4.team05.sb01otbooteam05.domain.clothes.dto.ClothesDto;
import com.part4.team05.sb01otbooteam05.domain.recommend.service.RecommendService;
import java.util.List;
import java.util.UUID;
import javax.security.sasl.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendController {

  private final RecommendService recommendService;

  @GetMapping
  public ResponseEntity<List<List<ClothesDto>>> getRecommendSet(@RequestParam UUID weatherId)
      throws AuthenticationException {
    UUID userId;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      userId = userDetails.getUserId();
    } else {
      throw new AuthenticationException("로그인 된 유저가 없습니다.");
    }

    return ResponseEntity.ok(recommendService.getRecommend(userId,weatherId));
  }

}
