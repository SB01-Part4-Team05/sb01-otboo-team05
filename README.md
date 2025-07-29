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

## 🛠️ 기술 스택
- **Backend:**
    - Spring Boot
    - Spring Data JPA
    - Spring Batch
    - Spring Scheduler
    - Spring Security
- **Database:**
    - PostgreSQL
- **Tool:**
    - Git & Github
    - Discord
    - Notion

## ✨ 주요 기능

## 🖥 팀원별 구현 기능 상세

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
