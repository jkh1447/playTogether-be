package com.jkh1447.MyProject.domain.matching;

public class MatchingConstants {

    public static final String USER_QUEUE_INFOS_KEY = "user:queue:infos";
    public static final String USER_QUEUE_STATUS_KEY = "user:queue:status";
    public static final int PIVOT_LIMIT = 10;
    public static final int SELF_LIMIT = 50;
    public static final int ANY_LIMIT = 30;
    public static final int ONLY_ANY_LIMIT = 80;
    public static final int ONLY_ANY_QUEUE_MIN_TEAM_SIZE = 2;

    public static final String AI_MATCHING = "AI_MATCHING";
    public static final String AI_USER_INPUT = "userInput";
    public static final String USER_CREATED_GAME_USER_INPUT = "userInput";

    public static final String MATCH_STATUS_KEY = "match:status:";
    public static final String ROOM_PARTICIPANTS_KEY = "room:participants:";
    public static final String ROOM_STATUS_KEY = "room:status:";
    public static final String ACTIVE_QUEUES_KEY = "active:queues";
    public static final String ACTIVE_ANY_QUEUE_KEY = "active:any:queues";
    public static final String MATCH_GROUP_SIZE = "groupSize";
    public static final String MATCH_ACCEPT_COUNT = "acceptCount";
    public static final String MATCH_PARTICIPANTS_DATA = "participantsData";
    public static final String QUEUE_KEY = "queue";
    public static final String MATCH_QUEUE_KEY = "queueKey";
    public static final String ANY_GROUP_SIZE = "any";
    public static final String MATCH_USER_QUEUE_INFO = "userQueueInfo";

    public static final String SUB_MATCH_FOUND_PATH = "/queue/match-found";
    public static final String SUB_MOVE_ROOM_PATH = "/queue/move-room";
    public static final String SUB_MATCH_DECLINE_PATH = "/queue/match-decline";

    public static final int GUEST_NICKNAME_LENGTH = 4;

    public static final String MATCH_STATUS_ACCEPTED = "ACCEPTED";
    public static final String MATCH_DECLINED_FLAG = "isDeclined";
    public static final String MATCH_ACCEPTED_PREFIX = "user:";

    public static final long ROOM_EXPIRE_SECONDS = 60 * 60 * 24; // 24 시간
    public static final int MATCH_STATUS_EXPIRE_SECONDS = 60; // 1 분
    public static final int MATCH_TIMEOUT_SECONDS = 11; // 11초
}
