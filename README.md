# 👕👖 옷장을 부탁해 🧥👗 (5팀)

![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/SB01-Part4-Team05/sb01-otboo-team05?utm_source=oss&utm_medium=github&utm_campaign=SB01-Part4-Team05%2Fsb01-otboo-team05&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews) [![codecov](https://codecov.io/github/SB01-Part4-Team05/sb01-otboo-team05/graph/badge.svg?token=QOS4VDHAIU)](https://codecov.io/github/SB01-Part4-Team05/sb01-otboo-team05)

> 날씨와 취향에 따라, 오늘의 스타일을 추천합니다. 
> 
> 실시간 날씨 데이터와 사용자 프로필을 바탕으로 개인 맞춤형 의상을 추천하고, 
> OOTD 공유, 팔로우, 실시간 DM까지 가능한 소셜 패션 커뮤니티 서비스입니다.

## 👨‍👩‍👧‍👦 팀원 구성

| 김희수 | 박지현 | 이민주 | 이성근 | 전민기 |
|:---:|:---:|:---:|:---:|:---:|
| <img src="https://avatars.githubusercontent.com/u/92302468?v=4" width="130"> | <img src="https://avatars.githubusercontent.com/u/146858227?v=4" width="130"> | <img src="https://avatars.githubusercontent.com/u/157027619?v=4" width="130"> | <img src="https://avatars.githubusercontent.com/u/61682044?v=4" width="130"> | <img src="https://avatars.githubusercontent.com/u/130732134?v=4" width="130"> |
| [kaya-frog-ramer](https://github.com/kaya-frog-ramer) | [jjhparkk](https://github.com/jjhparkk) | [m0276](https://github.com/m0276) | [LeeSG-0114](https://github.com/LeeSG-0114) | [mingi96](https://github.com/mingi96) |

## 🖥 팀원별 구현 기능
👤 김희수 *Back-End*
- 피드 도메인 구현
- PPT 최종 제작

👤 박지현 *Back-End*
- 사용자 및 프로필 도메인 구현
- 고정 도메인 설계 도입 및 유저 정책 정리 
- PPT 기초 제작, 노션 관리 등 협업 기반 정리

👤 이민주 *Back-End*
- 의상/의상 속성 도메인 구현 
- CI/CD 및 ECS, EC2 배포 파이프라인 구축

👤 이성근 *Back-End*, *팀장*
- 날씨 도메인 구현
- 프로젝트 일정 총괄

👤 전민기 *Back-End*
- 알림, 팔로우, DM 도메인 구현

## 🛠️ 기술 스택
- **Backend:**
    - Spring Boot
    - Spring Data JPA
    - Spring Batch
    - Spring Scheduler
    - Spring Security
    - Hugging Face
- **Database:**
    - PostgreSQL
- **Tool:**
    - Git & Github
    - Discord
    - Notion

## ✨ 주요 기능
✅ 사용자 관리
회원 가입 / 로그인 / 로그아웃 (JWT 기반 인증)

- 어드민 기능
- 계정 초기화 및 권한 관리 (USER / ADMIN)
- 계정 잠금 및 강제 로그아웃
- 비밀번호 초기화 (임시 비밀번호 발급 및 만료 처리)
- 소셜 로그인 연동 (Google, Kakao)

✅ 프로필 관리
- 사용자 정보 수정: 이름, 성별, 생년월일, 위치(x, y 좌표), 온도 민감도
- 위치 기반: 카카오 API로 행정 구역 이름 추출, 기상청 API용 좌표 연산

👕 의상 관리
- 어드민: 의상 속성 정의
- 사용자: 의상 등록 및 구매 링크(URL) 기반 자동 정보 추출 (무신사, 29CM 등)

🌤 날씨 데이터
- 기상청 단기 예보 API를 활용한 날씨 수집
- Spring Batch 기반 배치 처리 및 이상 기후 발생 시 알림 전송
- Spring Actuator로 배치 모니터링 및 메트릭 수집

🎽 의상 추천
- 날씨, 사용자 프로필, 의상 데이터를 활용한 맞춤 추천
- LLM API 연동을 통한 고도화된 추천 (HuggingFace)

🧾 OOTD 피드
- 추천 결과를 OOTD 피드로 등록
- 피드에 좋아요/댓글 작성 가능, 활동에 따른 실시간 알림 제공

👥 팔로우 & DM
- 사용자 팔로우 및 알림 
- 실시간 DM 기능 (웹소켓 기반), DM Key를 활용한 사용자 식별

🔔 실시간 알림
- SSE(Server Sent Event)를 통해 알림 전송
- 알림 종류: 댓글, 좋아요, 팔로우, DM, 권한 변경, 의상 속성 추가, 날씨 변동 등

## 📁 파일 구조
````
   src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      └─part4
    │  │          └─team05
    │  │              └─sb01otbooteam05
    │  │                  ├─config
    │  │                  ├─domain
    │  │                  │  ├─attribute
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  ├─auth
    │  │                  │  │  ├─config
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─repository
    │  │                  │  │  ├─security
    │  │                  │  │  │  ├─filter
    │  │                  │  │  │  ├─handler
    │  │                  │  │  │  └─jwt
    │  │                  │  │  └─service
    │  │                  │  ├─base
    │  │                  │  ├─clothes
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  ├─directMessage
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  │      └─impl
    │  │                  │  ├─feed
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  │  └─request
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─enums
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  ├─feedComment
    │  │                  │  │  ├─dto
    │  │                  │  │  │  └─request
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  ├─feedLike
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─mapper
    │  │                  │  │  └─repository
    │  │                  │  ├─follow
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  │      └─impl
    │  │                  │  ├─notification
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  │      └─impl
    │  │                  │  ├─ootd
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─mapper
    │  │                  │  │  ├─repository
    │  │                  │  │  └─service
    │  │                  │  ├─recommend
    │  │                  │  │  ├─controller
    │  │                  │  │  └─service
    │  │                  │  ├─user
    │  │                  │  │  ├─controller
    │  │                  │  │  ├─dto
    │  │                  │  │  ├─entity
    │  │                  │  │  ├─exception
    │  │                  │  │  ├─repository
    │  │                  │  │  ├─service
    │  │                  │  │  └─util
    │  │                  │  └─weather
    │  │                  │      ├─batch
    │  │                  │      │  ├─config
    │  │                  │      │  ├─listener
    │  │                  │      │  ├─processor
    │  │                  │      │  ├─reader
    │  │                  │      │  └─writer
    │  │                  │      ├─client
    │  │                  │      ├─controller
    │  │                  │      ├─dto
    │  │                  │      ├─entity
    │  │                  │      ├─exception
    │  │                  │      ├─mapper
    │  │                  │      ├─repository
    │  │                  │      ├─scheduler
    │  │                  │      ├─service
    │  │                  │      └─utils
    │  │                  └─exception
    │  └─resources
    │      ├─static
    │      │  └─assets
    │      └─templates
    └─test
        ├─java
        │  └─com
        │      └─part4
        │          └─team05
        │              └─sb01otbooteam05
        │                  └─domain
        │                      ├─attribute
        │                      │  ├─controller
        │                      │  └─service
        │                      ├─auth
        │                      │  └─service
        │                      ├─clothes
        │                      │  ├─controller
        │                      │  └─service
        │                      ├─directMessage
        │                      ├─feed
        │                      │  └─service
        │                      ├─feedComment
        │                      ├─feedLike
        │                      ├─follow
        │                      ├─notification
        │                      ├─ootd
        │                      ├─recommend
        │                      │  ├─controller
        │                      │  └─service
        │                      ├─user
        │                      │  └─service
        │                      └─weather
        │                          ├─client
        │                          └─service
        └─resources
