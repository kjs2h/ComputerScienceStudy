package com.csnote.config;

import com.csnote.domain.NoteCatalog;
import com.csnote.domain.NotePage;
import com.csnote.service.NoteScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NoteConfig {

    @Bean
    public NoteCatalog noteCatalog(NoteScanner scanner) {
        return scanner.scan(List.of(
                new NotePage("home", "표지", "노트 구성과 사용법"),
                new NotePage("summary", "요약", "과목별 핵심 · 외울 목록 · 헷갈리는 개념 · 비교 · 코드"),
                new NotePage("os", "운영체제", "프로세스 · 스케줄링 · 동기화 · 교착 · 메모리 · 파일 · I/O"),
                new NotePage("net", "네트워크", "OSI · TCP/IP · 라우팅 · HTTP · TLS · 로드밸런싱"),
                new NotePage("sec", "정보보안", "공격과 방어 · 암호 · 인증 · 웹 보안 · 보안 아키텍처"),
                new NotePage("arch", "컴퓨터구조", "CPU · 캐시 · 데이터 표현 · 논리회로 · 성능 · 저장장치"),
                new NotePage("algo", "알고리즘", "복잡도 · 정렬 · 탐색 · 그래프 · DP · 문제 풀이 패턴"),
                new NotePage("ds", "자료구조", "선형 구조 · 해시 · 트리 · 힙 · 그래프 · 고급 구조"),
                new NotePage("sw", "SW공학", "방법론 · 테스트 · OOP · 패턴 · 아키텍처 · 프로젝트 관리"),
                new NotePage("db", "데이터베이스", "트랜잭션 · SQL · 정규화 · 인덱스 · 동시성 · NoSQL"),
                new NotePage("ai", "AI", "머신러닝 · 딥러닝 · Transformer · LLM · RAG · MLOps"),
                new NotePage("prog", "프로그래밍", "Java · Python · C · 메모리 모델 · 언어 개념"),
                new NotePage("web", "웹·백엔드", "REST · 인증 · Spring · JPA · 캐시 · 메시지 큐"),
                new NotePage("infra", "클라우드·DevOps", "리눅스 · Git · Docker · Kubernetes · CI/CD · 관측성"),
                new NotePage("design", "시스템 설계", "확장성 · 캐시 · 분산 데이터 · 장애 내성 · 설계 예제"),
                new NotePage("appx", "부록", "계산 공식과 풀이 · 비교 인덱스 · 답안 작성법")
        ));
    }
}
