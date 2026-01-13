package com.competition.service;

import com.competition.entity.*;
import com.competition.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class InitDataService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionSkillRepository competitionSkillRepository;
    private final UserSkillRepository userSkillRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("开始初始化测试数据...");
            initTestData();
            System.out.println("测试数据初始化完成！");
            printLoginInfo();
        }
    }

    private void initTestData() {
        // 1. 初始化技能数据
        List<Skill> skills = initSkills();

        // 2. 初始化用户数据
        List<User> users = initUsers();

        // 3. 为用户分配技能
        initUserSkills(users, skills);

        // 4. 初始化竞赛数据
        List<Competition> competitions = initCompetitions();

        // 5. 为竞赛分配所需技能
        initCompetitionSkills(competitions, skills);

        // 6. 初始化队伍数据
        List<Team> teams = initTeams(users, competitions);

        // 7. 初始化队伍成员
        initTeamMembers(teams, users);

        // 8. 初始化用户行为数据
        initUserBehaviors(users, competitions, teams);
    }

    private List<Skill> initSkills() {
        List<Skill> skills = Arrays.asList(
                // 编程语言类
                createSkill("Java", "编程语言", "面向对象编程语言，企业级应用开发"),
                createSkill("Python", "编程语言", "通用编程语言，数据科学和AI开发"),
                createSkill("C++", "编程语言", "高性能编程语言，算法竞赛常用"),
                createSkill("JavaScript", "编程语言", "Web前端开发核心语言"),
                createSkill("Go", "编程语言", "现代并发编程语言"),
                createSkill("Rust", "编程语言", "系统级编程语言"),
                createSkill("C#", "编程语言", ".NET生态编程语言"),
                createSkill("PHP", "编程语言", "Web后端开发语言"),

                // 算法与数据结构
                createSkill("算法设计", "算法", "数据结构与算法设计分析"),
                createSkill("动态规划", "算法", "动态规划算法设计"),
                createSkill("图论算法", "算法", "图论相关算法实现"),
                createSkill("字符串算法", "算法", "字符串处理算法"),
                createSkill("数论算法", "算法", "数论相关算法"),

                // Web开发
                createSkill("前端开发", "Web开发", "HTML/CSS/JavaScript前端开发"),
                createSkill("React", "Web开发", "React框架开发"),
                createSkill("Vue.js", "Web开发", "Vue.js框架开发"),
                createSkill("Angular", "Web开发", "Angular框架开发"),
                createSkill("Node.js", "Web开发", "Node.js后端开发"),
                createSkill("Spring Boot", "Web开发", "Spring Boot框架开发"),
                createSkill("Django", "Web开发", "Django框架开发"),

                // 数据库
                createSkill("MySQL", "数据库", "MySQL关系型数据库"),
                createSkill("PostgreSQL", "数据库", "PostgreSQL数据库"),
                createSkill("MongoDB", "数据库", "MongoDB文档数据库"),
                createSkill("Redis", "数据库", "Redis内存数据库"),
                createSkill("数据库设计", "数据库", "关系型数据库设计与优化"),

                // 人工智能
                createSkill("机器学习", "人工智能", "机器学习算法与应用"),
                createSkill("深度学习", "人工智能", "深度神经网络"),
                createSkill("计算机视觉", "人工智能", "图像识别与处理"),
                createSkill("自然语言处理", "人工智能", "文本分析与处理"),
                createSkill("TensorFlow", "人工智能", "TensorFlow深度学习框架"),
                createSkill("PyTorch", "人工智能", "PyTorch深度学习框架"),

                // 数据科学
                createSkill("数据分析", "数据科学", "数据分析与可视化"),
                createSkill("数据挖掘", "数据科学", "数据挖掘技术"),
                createSkill("统计学", "数据科学", "统计学理论与应用"),
                createSkill("Pandas", "数据科学", "Python数据分析库"),
                createSkill("NumPy", "数据科学", "Python科学计算库"),

                // 数学建模
                createSkill("数学建模", "数学", "数学建模与仿真"),
                createSkill("运筹学", "数学", "运筹学优化方法"),
                createSkill("概率论", "数学", "概率论与数理统计"),
                createSkill("MATLAB", "数学", "MATLAB数值计算"),
                createSkill("R语言", "数学", "R语言统计分析"),

                // 设计类
                createSkill("UI设计", "设计", "用户界面设计"),
                createSkill("UX设计", "设计", "用户体验设计"),
                createSkill("平面设计", "设计", "平面视觉设计"),
                createSkill("产品设计", "设计", "产品设计与原型"),
                createSkill("3D建模", "设计", "三维建模设计"),

                // 管理类
                createSkill("项目管理", "管理", "项目管理与团队协作"),
                createSkill("团队协作", "管理", "团队沟通与协作"),
                createSkill("产品管理", "管理", "产品规划与管理"),
                createSkill("市场营销", "管理", "市场分析与营销策划"),

                // 其他技术
                createSkill("区块链", "新技术", "区块链技术与应用"),
                createSkill("云计算", "新技术", "云计算平台与服务"),
                createSkill("Docker", "DevOps", "容器化技术"),
                createSkill("Kubernetes", "DevOps", "容器编排技术"),
                createSkill("网络安全", "安全", "网络安全防护"),
                createSkill("渗透测试", "安全", "安全渗透测试")
        );

        return skillRepository.saveAll(skills);
    }

    private List<User> initUsers() {
        List<User> users = Arrays.asList(
                // 管理员账户
                createUser("admin", "admin@competition.com", "系统管理员", "平台管理",
                        "计算机科学与技术", "教师", "13800000000"),

                // 学生账户 - 编程类
                createUser("zhangsan", "zhangsan@stu.edu.cn", "张三", "清华大学",
                        "计算机科学与技术", "大三", "13800000001"),
                createUser("lisi", "lisi@stu.edu.cn", "李四", "北京大学",
                        "软件工程", "大二", "13800000002"),
                createUser("wangwu", "wangwu@stu.edu.cn", "王五", "复旦大学",
                        "数据科学与大数据技术", "大四", "13800000003"),
                createUser("zhaoliu", "zhaoliu@stu.edu.cn", "赵六", "上海交通大学",
                        "人工智能", "研一", "13800000004"),
                createUser("sunqi", "sunqi@stu.edu.cn", "孙七", "浙江大学",
                        "计算机科学与技术", "大三", "13800000005"),

                // 学生账户 - 数学建模类
                createUser("chenba", "chenba@stu.edu.cn", "陈八", "中南大学",
                        "应用数学", "大二", "13800000006"),
                createUser("zhoujiu", "zhoujiu@stu.edu.cn", "周九", "华中科技大学",
                        "统计学", "大三", "13800000007"),
                createUser("wushi", "wushi@stu.edu.cn", "吴十", "西安交通大学",
                        "数学与应用数学", "大四", "13800000008"),

                // 学生账户 - 设计类
                createUser("liuyi", "liuyi@stu.edu.cn", "刘一", "中央美术学院",
                        "视觉传达设计", "大二", "13800000009"),
                createUser("xueer", "xueer@stu.edu.cn", "薛二", "清华大学",
                        "工业设计", "大三", "13800000010"),

                // 学生账户 - 创业类
                createUser("yangsan", "yangsan@stu.edu.cn", "杨三", "北京理工大学",
                        "工商管理", "大四", "13800000011"),
                createUser("maosi", "maosi@stu.edu.cn", "毛四", "同济大学",
                        "市场营销", "研二", "13800000012"),

                // 学生账户 - 综合类
                createUser("hanwu", "hanwu@stu.edu.cn", "韩五", "东南大学",
                        "电子信息工程", "大三", "13800000013"),
                createUser("caoliu", "caoliu@stu.edu.cn", "曹六", "华南理工大学",
                        "自动化", "大二", "13800000014"),
                createUser("jiangqi", "jiangqi@stu.edu.cn", "江七", "大连理工大学",
                        "机械工程", "大四", "13800000015"),

                // 更多学生账户
                createUser("fengba", "fengba@stu.edu.cn", "冯八", "北京航空航天大学",
                        "航空航天工程", "研一", "13800000016"),
                createUser("dujiu", "dujiu@stu.edu.cn", "杜九", "电子科技大学",
                        "通信工程", "大三", "13800000017"),
                createUser("yeshi", "yeshi@stu.edu.cn", "叶十", "南京大学",
                        "物理学", "大二", "13800000018"),
                createUser("xuyi", "xuyi@stu.edu.cn", "许一", "中山大学",
                        "生物信息学", "大四", "13800000019"),
                createUser("heer", "heer@stu.edu.cn", "何二", "哈尔滨工业大学",
                        "材料科学与工程", "研二", "13800000020")
        );

        return userRepository.saveAll(users);
    }

    private void initUserSkills(List<User> users, List<Skill> skills) {
        Random random = new Random();

        // 为每个用户分配3-8个技能
        for (User user : users) {
            List<Skill> userSkills = new ArrayList<>();
            int skillCount = 3 + random.nextInt(6); // 3-8个技能

            // 根据用户专业分配相关技能
            String major = user.getMajor();
            if (major.contains("计算机") || major.contains("软件")) {
                userSkills.addAll(getSkillsByCategory(skills, "编程语言", 2, 4));
                userSkills.addAll(getSkillsByCategory(skills, "算法", 1, 3));
                userSkills.addAll(getSkillsByCategory(skills, "Web开发", 1, 2));
            } else if (major.contains("数学") || major.contains("统计")) {
                userSkills.addAll(getSkillsByCategory(skills, "数学", 2, 4));
                userSkills.addAll(getSkillsByCategory(skills, "数据科学", 1, 3));
                userSkills.addAll(getSkillsByCategory(skills, "编程语言", 1, 2));
            } else if (major.contains("设计")) {
                userSkills.addAll(getSkillsByCategory(skills, "设计", 2, 4));
                userSkills.addAll(getSkillsByCategory(skills, "管理", 0, 2));
            } else if (major.contains("管理") || major.contains("营销")) {
                userSkills.addAll(getSkillsByCategory(skills, "管理", 2, 4));
                userSkills.addAll(getSkillsByCategory(skills, "设计", 0, 2));
            } else {
                // 其他专业随机分配
                userSkills.addAll(getRandomSkills(skills, skillCount));
            }

            // 去重并限制数量
            userSkills = userSkills.stream().distinct().collect(java.util.stream.Collectors.toList());

            // 保存用户技能
            for (int i = 0; i < Math.min(skillCount, userSkills.size()); i++) {
                Skill skill = userSkills.get(i);
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkill.setProficiency(1 + random.nextInt(5)); // 1-5熟练度
                userSkillRepository.save(userSkill);
            }
        }
    }

    private List<Competition> initCompetitions() {
        List<Competition> competitions = Arrays.asList(
                // 编程类竞赛
                createCompetition(
                        "ACM国际大学生程序设计竞赛",
                        "全球最具影响力的大学生程序设计竞赛，考验学生的算法设计与编程实现能力。比赛采用团队形式，需要在限定时间内解决多个算法问题。",
                        "ACM", "编程", "国际级",
                        LocalDate.now().plusDays(45), LocalDate.now().plusDays(47), LocalDate.now().plusDays(30), 3
                ),
                createCompetition(
                        "蓝桥杯全国软件和信息技术专业人才大赛",
                        "面向全国大学生的软件开发技能竞赛，包含算法设计、软件开发等多个方向。旨在培养和发现软件开发人才。",
                        "工业和信息化部", "编程", "国家级",
                        LocalDate.now().plusDays(20), LocalDate.now().plusDays(20), LocalDate.now().plusDays(10), 1
                ),
                createCompetition(
                        "中国大学生程序设计竞赛(CCPC)",
                        "中国最高水平的大学生程序设计竞赛之一，与ACM-ICPC并列为国内最权威的编程竞赛。",
                        "中国计算机学会", "编程", "国家级",
                        LocalDate.now().plusDays(60), LocalDate.now().plusDays(62), LocalDate.now().plusDays(45), 3
                ),

                // 数学建模类竞赛
                createCompetition(
                        "全国大学生数学建模竞赛",
                        "培养学生创新意识及运用数学方法和计算机技术解决实际问题的能力。参赛队伍需要在72小时内完成一篇数学建模论文。",
                        "中国工业与应用数学学会", "数学建模", "国家级",
                        LocalDate.now().plusDays(90), LocalDate.now().plusDays(93), LocalDate.now().plusDays(75), 3
                ),
                createCompetition(
                        "美国大学生数学建模竞赛(MCM/ICM)",
                        "国际权威的数学建模竞赛，分为MCM和ICM两个方向，考验学生的数学建模、问题分析和论文写作能力。",
                        "美国数学及其应用联合会", "数学建模", "国际级",
                        LocalDate.now().plusDays(120), LocalDate.now().plusDays(124), LocalDate.now().plusDays(105), 3
                ),
                createCompetition(
                        "华为杯中国研究生数学建模竞赛",
                        "面向研究生的高水平数学建模竞赛，题目更具挑战性和实用性，与产业结合紧密。",
                        "中国学位与研究生教育学会", "数学建模", "国家级",
                        LocalDate.now().plusDays(150), LocalDate.now().plusDays(153), LocalDate.now().plusDays(135), 3
                ),

                // 创新创业类竞赛
                createCompetition(
                        "中国国际'互联网+'大学生创新创业大赛",
                        "激发大学生创新创业热情，培养造就'大众创业、万众创新'生力军。涵盖互联网、人工智能、新能源等多个领域。",
                        "教育部", "创新创业", "国家级",
                        LocalDate.now().plusDays(180), LocalDate.now().plusDays(185), LocalDate.now().plusDays(160), 5
                ),
                createCompetition(
                        "挑战杯全国大学生课外学术科技作品竞赛",
                        "被誉为中国大学生学术科技'奥林匹克'，旨在培养大学生创新精神和实践能力。",
                        "共青团中央", "创新创业", "国家级",
                        LocalDate.now().plusDays(200), LocalDate.now().plusDays(205), LocalDate.now().plusDays(180), 5
                ),
                createCompetition(
                        "创青春全国大学生创业大赛",
                        "全国性大学生创业竞赛，包括创业计划竞赛、创业实践挑战赛和公益创业赛三个组别。",
                        "共青团中央", "创新创业", "国家级",
                        LocalDate.now().plusDays(220), LocalDate.now().plusDays(225), LocalDate.now().plusDays(200), 4
                ),

                // 设计类竞赛
                createCompetition(
                        "全国大学生工业设计大赛",
                        "面向全国大学生的工业设计专业竞赛，旨在培养设计创新人才，推动设计教育发展。",
                        "教育部", "设计", "国家级",
                        LocalDate.now().plusDays(100), LocalDate.now().plusDays(105), LocalDate.now().plusDays(80), 3
                ),
                createCompetition(
                        "中国大学生广告艺术节学院奖",
                        "中国大学生广告艺术领域最高奖项，涵盖平面、影视、数字媒体等多个广告形式。",
                        "中国广告协会", "设计", "国家级",
                        LocalDate.now().plusDays(130), LocalDate.now().plusDays(135), LocalDate.now().plusDays(110), 4
                ),

                // 人工智能类竞赛
                createCompetition(
                        "全国大学生人工智能创新大赛",
                        "面向人工智能技术应用的创新竞赛，包括机器学习、深度学习、计算机视觉等方向。",
                        "工业和信息化部", "人工智能", "国家级",
                        LocalDate.now().plusDays(80), LocalDate.now().plusDays(85), LocalDate.now().plusDays(60), 4
                ),
                createCompetition(
                        "百度之星程序设计大赛",
                        "由百度公司举办的算法编程竞赛，包含算法赛和AI赛两个方向，注重实际应用。",
                        "百度公司", "人工智能", "企业级",
                        LocalDate.now().plusDays(40), LocalDate.now().plusDays(42), LocalDate.now().plusDays(25), 1
                ),

                // 网络安全类竞赛
                createCompetition(
                        "全国大学生信息安全竞赛",
                        "培养大学生信息安全意识和技能，包括网络攻防、密码学、安全编程等多个方向。",
                        "教育部", "网络安全", "国家级",
                        LocalDate.now().plusDays(110), LocalDate.now().plusDays(113), LocalDate.now().plusDays(90), 3
                ),

                // 电子设计类竞赛
                createCompetition(
                        "全国大学生电子设计竞赛",
                        "面向大学生的电子技术应用竞赛，考验学生的电路设计、编程和系统集成能力。",
                        "教育部", "电子设计", "国家级",
                        LocalDate.now().plusDays(140), LocalDate.now().plusDays(144), LocalDate.now().plusDays(120), 3
                ),

                // 机器人竞赛
                createCompetition(
                        "全国大学生机器人大赛",
                        "综合性机器人竞赛，包括机械设计、电子控制、编程算法等多个技术领域。",
                        "共青团中央", "机器人", "国家级",
                        LocalDate.now().plusDays(170), LocalDate.now().plusDays(175), LocalDate.now().plusDays(150), 5
                ),

                // 校级竞赛示例
                createCompetition(
                        "清华大学程序设计竞赛",
                        "清华大学校内程序设计竞赛，为ACM等国际竞赛选拔和培养人才。",
                        "清华大学", "编程", "校级",
                        LocalDate.now().plusDays(15), LocalDate.now().plusDays(15), LocalDate.now().plusDays(5), 3
                ),
                createCompetition(
                        "北京大学创新创业大赛",
                        "北京大学校内创新创业竞赛，鼓励学生创新思维和创业实践。",
                        "北京大学", "创新创业", "校级",
                        LocalDate.now().plusDays(35), LocalDate.now().plusDays(40), LocalDate.now().plusDays(20), 4
                )
        );

        return competitionRepository.saveAll(competitions);
    }

    private void initCompetitionSkills(List<Competition> competitions, List<Skill> skills) {
        Random random = new Random();

        for (Competition competition : competitions) {
            List<CompetitionSkill> competitionSkills = new ArrayList<>();

            // 根据竞赛类别分配所需技能
            String category = competition.getCategory();
            switch (category) {
                case "编程":
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "编程语言", 1, 3), 4, 5));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "算法", 2, 4), 5, 5));
                    break;
                case "数学建模":
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "数学", 2, 4), 5, 5));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "编程语言", 1, 2), 3, 4));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "数据科学", 1, 2), 3, 4));
                    break;
                case "创新创业":
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "管理", 2, 4), 4, 5));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "设计", 1, 2), 3, 4));
                    break;
                case "设计":
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "设计", 2, 4), 5, 5));
                    break;
                case "人工智能":
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "人工智能", 2, 4), 5, 5));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            getSkillsByCategory(skills, "编程语言", 1, 2), 4, 4));
                    break;
                default:
                    // 其他类别随机分配技能
                    List<Skill> randomSkills = getRandomSkills(skills, 3 + random.nextInt(3));
                    competitionSkills.addAll(createCompetitionSkills(competition,
                            randomSkills, 3, 5));
                    break;
            }

            competitionSkillRepository.saveAll(competitionSkills);
        }
    }

    private List<Team> initTeams(List<User> users, List<Competition> competitions) {
        List<Team> teams = new ArrayList<>();
        Random random = new Random();

        // 为每个竞赛创建1-3个队伍
        for (Competition competition : competitions) {
            int teamCount = 1 + random.nextInt(3); // 1-3个队伍

            for (int i = 0; i < teamCount && i < users.size(); i++) { // 添加用户数量检查
                // 随机选择队长
                User leader = users.get(random.nextInt(users.size()));

                Team team = new Team();
                team.setName(generateTeamName(competition.getCategory(), i + 1));
                team.setDescription(generateTeamDescription(competition.getCategory()));
                team.setCompetition(competition);
                team.setLeader(leader);
                team.setMaxMembers(competition.getMaxTeamSize());
                team.setCurrentMembers(1);
                team.setStatus(Team.TeamStatus.RECRUITING);

                teams.add(teamRepository.save(team));
            }
        }

        return teams;
    }

    private void initTeamMembers(List<Team> teams, List<User> users) {
        Random random = new Random();

        for (Team team : teams) {
            // 队长作为第一个成员
            TeamMember leaderMember = new TeamMember();
            leaderMember.setTeam(team);
            leaderMember.setUser(team.getLeader());
            leaderMember.setRole(TeamMember.Role.LEADER);
            teamMemberRepository.save(leaderMember);

            // 随机添加其他成员
            int maxAdditionalMembers = team.getMaxMembers() - 1; // 减去队长

            // 修复：确保 maxAdditionalMembers 大于 0
            if (maxAdditionalMembers > 0) {
                int additionalMembers = random.nextInt(maxAdditionalMembers + 1); // +1 是因为 nextInt 不包含上界
                List<User> availableUsers = new ArrayList<>(users);
                availableUsers.remove(team.getLeader()); // 移除队长

                for (int i = 0; i < additionalMembers && !availableUsers.isEmpty(); i++) {
                    User member = availableUsers.remove(random.nextInt(availableUsers.size()));

                    TeamMember teamMember = new TeamMember();
                    teamMember.setTeam(team);
                    teamMember.setUser(member);
                    teamMember.setRole(TeamMember.Role.MEMBER);
                    teamMemberRepository.save(teamMember);

                    // 更新队伍成员数量
                    team.setCurrentMembers(team.getCurrentMembers() + 1);
                }
            }

            // 更新队伍状态
            if (team.getMaxMembers() != null && team.getCurrentMembers() >= team.getMaxMembers()) {
                team.setStatus(Team.TeamStatus.CLOSED);
            }
            teamRepository.save(team);
        }
    }

    private void initUserBehaviors(List<User> users, List<Competition> competitions, List<Team> teams) {
        Random random = new Random();

        // 为每个用户生成行为数据
        for (User user : users) {
            // 生成竞赛浏览行为
            if (!competitions.isEmpty()) {
                int competitionViewCount = Math.min(5 + random.nextInt(10), competitions.size()); // 限制不超过竞赛总数
                List<Competition> shuffledCompetitions = new ArrayList<>(competitions);
                java.util.Collections.shuffle(shuffledCompetitions);

                for (int i = 0; i < competitionViewCount; i++) {
                    Competition competition = shuffledCompetitions.get(i);

                    // 浏览行为
                    createUserBehavior(user, UserBehavior.TargetType.COMPETITION,
                            competition.getId(), UserBehavior.BehaviorType.VIEW);

                    // 30%概率点赞
                    if (random.nextDouble() < 0.3) {
                        createUserBehavior(user, UserBehavior.TargetType.COMPETITION,
                                competition.getId(), UserBehavior.BehaviorType.LIKE);
                    }
                }
            }

            // 生成队伍浏览行为
            if (!teams.isEmpty()) {
                int teamViewCount = Math.min(3 + random.nextInt(8), teams.size()); // 限制不超过队伍总数
                List<Team> shuffledTeams = new ArrayList<>(teams);
                java.util.Collections.shuffle(shuffledTeams);

                for (int i = 0; i < teamViewCount; i++) {
                    Team team = shuffledTeams.get(i);

                    // 浏览行为
                    createUserBehavior(user, UserBehavior.TargetType.TEAM,
                            team.getId(), UserBehavior.BehaviorType.VIEW);

                    // 20%概率申请加入（如果不是队长）
                    if (random.nextDouble() < 0.2 && !team.getLeader().getId().equals(user.getId())) {
                        createUserBehavior(user, UserBehavior.TargetType.TEAM,
                                team.getId(), UserBehavior.BehaviorType.APPLY);
                    }
                }
            }
        }
    }

    // 辅助方法
    private Skill createSkill(String name, String category, String description) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        skill.setDescription(description);
        return skill;
    }

    private User createUser(String username, String email, String realName,
                            String school, String major, String grade, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setRealName(realName);
        user.setSchool(school);
        user.setMajor(major);
        user.setGrade(grade);
        user.setPhone(phone);
        return user;
    }

    private Competition createCompetition(String name, String description, String organizer,
                                          String category, String level, LocalDate startDate,
                                          LocalDate endDate, LocalDate regDeadline, int maxTeamSize) {
        Competition competition = new Competition();
        competition.setName(name);
        competition.setDescription(description);
        competition.setOrganizer(organizer);
        competition.setCategory(category);
        competition.setLevel(level);
        competition.setStartDate(startDate);
        competition.setEndDate(endDate);
        competition.setRegistrationDeadline(regDeadline);
        competition.setMaxTeamSize(maxTeamSize);
        competition.setStatus(Competition.CompetitionStatus.UPCOMING);
        return competition;
    }

    private List<Skill> getSkillsByCategory(List<Skill> skills, String category, int min, int max) {
        List<Skill> categorySkills = skills.stream()
                .filter(skill -> skill.getCategory().equals(category))
                .collect(java.util.stream.Collectors.toList());

        if (categorySkills.isEmpty()) {
            return new ArrayList<>();
        }

        java.util.Collections.shuffle(categorySkills);
        Random random = new Random();

        // 修复：确保 max >= min 且都不小于0
        int safeMin = Math.max(0, min);
        int safeMax = Math.max(safeMin, max);

        int count;
        if (safeMax == safeMin) {
            count = safeMin;
        } else {
            count = safeMin + random.nextInt(safeMax - safeMin + 1);
        }

        count = Math.min(count, categorySkills.size());

        return categorySkills.subList(0, count);
    }

    private List<Skill> getRandomSkills(List<Skill> skills, int count) {
        List<Skill> shuffled = new ArrayList<>(skills);
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private List<CompetitionSkill> createCompetitionSkills(Competition competition,
                                                           List<Skill> skills,
                                                           int minImportance,
                                                           int maxImportance) {
        List<CompetitionSkill> competitionSkills = new ArrayList<>();
        Random random = new Random();

        for (Skill skill : skills) {
            CompetitionSkill cs = new CompetitionSkill();
            cs.setCompetition(competition);
            cs.setSkill(skill);

            // 修复：确保 maxImportance >= minImportance
            int safeMin = Math.max(1, minImportance);
            int safeMax = Math.max(safeMin, maxImportance);

            int importance;
            if (safeMax == safeMin) {
                importance = safeMin;
            } else {
                importance = safeMin + random.nextInt(safeMax - safeMin + 1);
            }

            cs.setImportance(importance);
            competitionSkills.add(cs);
        }

        return competitionSkills;
    }

    private String generateTeamName(String category, int index) {
        String[] prefixes = {"梦想", "创新", "飞跃", "超越", "巅峰", "精英", "卓越", "先锋"};
        String[] suffixes = {"战队", "团队", "小组", "联盟", "工作室"};

        Random random = new Random();
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];

        return prefix + suffix + index;
    }

    private String generateTeamDescription(String category) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("编程", "我们是一支热爱编程的团队，擅长算法设计和代码实现，希望在竞赛中展现技术实力。");
        descriptions.put("数学建模", "专注于数学建模和数据分析，团队成员具备扎实的数学基础和编程能力。");
        descriptions.put("创新创业", "充满创新精神的创业团队，致力于将创意转化为现实，改变世界。");
        descriptions.put("设计", "设计驱动的创意团队，注重用户体验和视觉表现，追求完美的设计作品。");
        descriptions.put("人工智能", "专注于AI技术研究和应用的团队，在机器学习和深度学习方面有丰富经验。");

        return descriptions.getOrDefault(category, "我们是一支充满激情的团队，期待在竞赛中取得优异成绩。");
    }

    private void createUserBehavior(User user, UserBehavior.TargetType targetType,
                                    Long targetId, UserBehavior.BehaviorType behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUser(user);
        behavior.setTargetType(targetType);
        behavior.setTargetId(targetId);
        behavior.setBehaviorType(behaviorType);
        userBehaviorRepository.save(behavior);
    }

    private void printLoginInfo() {
//        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 学科竞赛管理平台初始化完成！");
//        System.out.println("=".repeat(60));
        System.out.println("📊 数据统计:");
        System.out.println("   👥 用户数量: " + userRepository.count());
        System.out.println("   🏆 竞赛数量: " + competitionRepository.count());
        System.out.println("   👨‍👩‍👧‍👦 队伍数量: " + teamRepository.count());
        System.out.println("   🛠️ 技能数量: " + skillRepository.count());
        System.out.println();
        System.out.println("🔑 测试账户信息:");
        System.out.println("   管理员: admin / 123456");
        System.out.println("   学生1: zhangsan / 123456 (张三 - 清华大学)");
        System.out.println("   学生2: lisi / 123456 (李四 - 北京大学)");
        System.out.println("   学生3: wangwu / 123456 (王五 - 复旦大学)");
        System.out.println("   学生4: zhaoliu / 123456 (赵六 - 上海交通大学)");
        System.out.println("   ... 更多账户请查看数据库");
        System.out.println();
        System.out.println("🌐 访问地址:");
        System.out.println("   前端: http://localhost:3000");
        System.out.println("   后端: http://localhost:8080");
//        System.out.println("=".repeat(60));
    }
}
