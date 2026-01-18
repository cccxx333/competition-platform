import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  List, 
  Button, 
  Tag, 
  Progress,
  Timeline,
  Avatar,
  Tooltip,
  Empty,
  Spin
} from 'antd';
import { 
  TrophyOutlined, 
  TeamOutlined, 
  UserOutlined,
  StarOutlined,
  CalendarOutlined,
  FireOutlined,
  RiseOutlined,
  ClockCircleOutlined
} from '@ant-design/icons';
import { competitionAPI, teamAPI, recommendationAPI, userAPI } from '../../services/api';
import { formatDate, getTimeFromNow } from '../../utils/format';
import moment from 'moment';

const Home = () => {
  const [loading, setLoading] = useState(false);
  const [statistics, setStatistics] = useState({
    totalCompetitions: 0,
    activeCompetitions: 0,
    totalTeams: 0,
    myTeams: 0,
    totalUsers: 0,
    myRecommendations: 0
  });
  const [recentCompetitions, setRecentCompetitions] = useState([]);
  const [recommendedCompetitions, setRecommendedCompetitions] = useState([]);
  const [hotTeams, setHotTeams] = useState([]);
  const [recentActivities, setRecentActivities] = useState([]);
  const [userProfile, setUserProfile] = useState({});

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // 并行加载所有数据
      const [
        competitions, 
        teams, 
        recommendations, 
        profile
      ] = await Promise.all([
        competitionAPI.getCompetitions({ page: 0, size: 8 }).catch(() => ({ content: [], totalElements: 0 })),
        teamAPI.getTeams({ page: 0, size: 6 }).catch(() => ({ content: [], totalElements: 0 })),
        recommendationAPI.getCompetitionRecommendations(6).catch(() => []),
        userAPI.getProfile().catch(() => ({}))
      ]);

      // 设置统计数据
      setStatistics({
        totalCompetitions: competitions.totalElements || 0,
        activeCompetitions: (competitions.content || []).filter(c => c?.status === 'ONGOING').length,
        totalTeams: teams.totalElements || 0,
        myTeams: (teams.content || []).filter(t => t?.leader?.id === profile?.id).length,
        totalUsers: 150, // 模拟数据
        myRecommendations: (recommendations || []).length
      });

      setRecentCompetitions(competitions.content || []);
      setRecommendedCompetitions(recommendations || []);
      setHotTeams((teams.content || []).slice(0, 4));
      setUserProfile(profile || {});
      
      // 生成模拟活动数据
      generateRecentActivities(competitions.content || [], teams.content || []);
      
    } catch (error) {
      console.error('加载仪表板数据失败:', error);
      // 设置默认空数据
      setRecentCompetitions([]);
      setRecommendedCompetitions([]);
      setHotTeams([]);
      setUserProfile({});
    } finally {
      setLoading(false);
    }
  };

  const generateRecentActivities = (competitions, teams) => {
    const activities = [];
    
    if (competitions.length > 0 && competitions[0]) {
      activities.push({
        type: 'competition',
        title: '新竞赛发布',
        description: `${competitions[0].name || '未知竞赛'} 开始报名`,
        time: '2小时前',
        icon: <TrophyOutlined style={{ color: '#1890ff' }} />
      });
    }
    
    if (teams.length > 0 && teams[0]) {
      activities.push({
        type: 'team',
        title: '队伍招募',
        description: `${teams[0].name || '未知队伍'} 正在招募成员`,
        time: '4小时前',
        icon: <TeamOutlined style={{ color: '#52c41a' }} />
      });
    }
    
    activities.push(
      {
        type: 'recommendation',
        title: '推荐更新',
        description: '为您推荐了新的竞赛',
        time: '6小时前',
        icon: <StarOutlined style={{ color: '#faad14' }} />
      },
      {
        type: 'system',
        title: '系统通知',
        description: 'ACM竞赛报名即将截止',
        time: '1天前',
        icon: <ClockCircleOutlined style={{ color: '#f5222d' }} />
      }
    );
    
    setRecentActivities(activities);
  };

  const StatisticCard = ({ title, value, icon, color, suffix = '' }) => (
    <Card hoverable>
      <Statistic
        title={title}
        value={value}
        prefix={React.cloneElement(icon, { style: { color } })}
        suffix={suffix}
        valueStyle={{ color, fontSize: '24px', fontWeight: 'bold' }}
      />
    </Card>
  );

  // 安全的竞赛渲染函数
  const renderCompetitionItem = (competition) => {
    if (!competition) {
      return null;
    }

    return (
      <List.Item style={{ padding: '12px 0' }}>
        <List.Item.Meta
          avatar={
            <Avatar 
              shape="square" 
              size={48}
              style={{ 
                backgroundColor: '#1890ff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <TrophyOutlined style={{ fontSize: '20px' }} />
            </Avatar>
          }
          title={
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{ fontWeight: 'bold' }}>{competition.name || '未知竞赛'}</span>
              <Tag color="blue">{competition.level || '未知级别'}</Tag>
            </div>
          }
          description={
            <div>
              <div style={{ marginBottom: '4px', fontSize: '12px', color: '#666' }}>
                {competition.organizer || '未知主办方'}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <Tag color="green" size="small">{competition.category || '未知分类'}</Tag>
                <span style={{ fontSize: '12px', color: '#999' }}>
                  <CalendarOutlined /> 
                  {competition.registrationDeadline ? 
                    formatDate(competition.registrationDeadline, 'MM-DD') + ' 截止' : 
                    '截止时间待定'
                  }
                </span>
              </div>
            </div>
          }
        />
      </List.Item>
    );
  };

  // 安全的推荐渲染函数
  const renderRecommendationItem = (recommendation) => {
    if (!recommendation || !recommendation.item) {
      return null;
    }

    const competition = recommendation.item;
    const score = recommendation.score || 0;

    return (
      <List.Item style={{ padding: '12px 0' }}>
        <List.Item.Meta
          avatar={
            <Avatar 
              shape="square" 
              size={48}
              style={{ 
                backgroundColor: '#faad14',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <StarOutlined style={{ fontSize: '20px' }} />
            </Avatar>
          }
          title={
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{ fontWeight: 'bold' }}>{competition.name || '未知竞赛'}</span>
              <Progress 
                type="circle" 
                size={32}
                percent={Math.round(score * 100)} 
                format={percent => `${percent}%`}
                strokeColor="#faad14"
              />
            </div>
          }
          description={
            <div>
              <div style={{ marginBottom: '4px', fontSize: '12px', color: '#52c41a' }}>
                {recommendation.explanation || '基于您的技能匹配推荐'}
              </div>
              <Tag color="orange" size="small">
                匹配度: {Math.round(score * 100)}%
              </Tag>
            </div>
          }
        />
      </List.Item>
    );
  };

  // 安全的队伍渲染函数
  const renderTeamItem = (team) => {
    if (!team) {
      return null;
    }

    return (
      <List.Item 
        style={{ padding: '12px 0' }}
        actions={[
          team.status === 'RECRUITING' ? (
            <Button type="primary" size="small">申请加入</Button>
          ) : (
            <Button size="small" disabled>已满员</Button>
          )
        ]}
      >
        <List.Item.Meta
          avatar={
            <Avatar 
              shape="square" 
              size={48}
              style={{ 
                backgroundColor: '#52c41a',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <TeamOutlined style={{ fontSize: '20px' }} />
            </Avatar>
          }
          title={
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{ fontWeight: 'bold' }}>{team.name || '未知队伍'}</span>
              <Tag color={team.status === 'RECRUITING' ? 'green' : 'orange'}>
                {team.currentMembers || 0}/{team.maxMembers || 0}人
              </Tag>
            </div>
          }
          description={
            <div>
              <div style={{ marginBottom: '4px', fontSize: '12px' }}>
                {team.competition?.name || '未知竞赛'}
              </div>
              <div style={{ fontSize: '12px', color: '#999' }}>
                队长: {team.leader?.realName || team.leader?.username || '未知'}
              </div>
            </div>
          }
        />
      </List.Item>
    );
  };

  return (
    <div style={{ padding: '0 0 24px 0' }}>
      {/* 欢迎横幅 */}
      <Card 
        style={{ 
          marginBottom: '24px',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white'
        }}
      >
        <Row align="middle">
          <Col flex="auto">
            <h2 style={{ color: 'white', margin: 0 }}>
              👋 欢迎回来，{userProfile.realName || userProfile.username || '用户'}！
            </h2>
            <p style={{ color: 'rgba(255,255,255,0.8)', margin: '8px 0 0 0' }}>
              今天是 {moment().format('YYYY年MM月DD日')}，
              当前有 {statistics.activeCompetitions} 个竞赛正在进行中
            </p>
          </Col>
          <Col>
            <Avatar size={64} src={userProfile.avatarUrl} icon={<UserOutlined />} />
          </Col>
        </Row>
      </Card>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            title="总竞赛数"
            value={statistics.totalCompetitions}
            icon={<TrophyOutlined />}
            color="#1890ff"
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            title="进行中"
            value={statistics.activeCompetitions}
            icon={<FireOutlined />}
            color="#52c41a"
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            title="我的队伍"
            value={statistics.myTeams}
            icon={<TeamOutlined />}
            color="#722ed1"
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <StatisticCard
            title="推荐匹配"
            value={statistics.myRecommendations}
            icon={<RiseOutlined />}
            color="#eb2f96"
          />
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 最新竞赛 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span>
                <TrophyOutlined style={{ marginRight: '8px', color: '#1890ff' }} />
                最新竞赛
              </span>
            }
            extra={<Button type="link" href="/competitions">查看全部</Button>}
            loading={loading}
          >
            {recentCompetitions.length > 0 ? (
              <List
                dataSource={recentCompetitions.slice(0, 4)}
                renderItem={renderCompetitionItem}
              />
            ) : (
              <Empty description="暂无竞赛数据" />
            )}
          </Card>
        </Col>

        {/* 智能推荐 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span>
                <StarOutlined style={{ marginRight: '8px', color: '#faad14' }} />
                为您推荐
              </span>
            }
            extra={<Button type="link" href="/recommendations">查看更多</Button>}
            loading={loading}
          >
            {recommendedCompetitions.length > 0 ? (
              <List
                dataSource={recommendedCompetitions.slice(0, 4)}
                renderItem={renderRecommendationItem}
              />
            ) : (
              <Empty description="暂无推荐数据" />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: '16px' }}>
        {/* 热门队伍 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span>
                <TeamOutlined style={{ marginRight: '8px', color: '#52c41a' }} />
                热门队伍
              </span>
            }
            extra={<Button type="link" href="/teams">查看全部</Button>}
          >
            {hotTeams.length > 0 ? (
              <List
                dataSource={hotTeams}
                renderItem={renderTeamItem}
              />
            ) : (
              <Empty description="暂无队伍数据" />
            )}
          </Card>
        </Col>

        {/* 最近动态 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span>
                <ClockCircleOutlined style={{ marginRight: '8px', color: '#722ed1' }} />
                最近动态
              </span>
            }
          >
            {recentActivities.length > 0 ? (
              <Timeline>
                {recentActivities.map((activity, index) => (
                  <Timeline.Item key={index} dot={activity.icon}>
                    <div style={{ fontSize: '14px', fontWeight: 'bold' }}>
                      {activity.title}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', margin: '4px 0' }}>
                      {activity.description}
                    </div>
                    <div style={{ fontSize: '12px', color: '#999' }}>
                      {activity.time}
                    </div>
                  </Timeline.Item>
                ))}
              </Timeline>
            ) : (
              <Empty description="暂无动态" />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Home;