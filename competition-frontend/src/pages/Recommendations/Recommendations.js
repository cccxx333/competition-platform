import React, { useState, useEffect } from 'react';
import { Card, List, Button, Tag, Rate, Spin, message, Tabs, Empty, Alert } from 'antd';
import { TrophyOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons';
import { recommendationAPI } from '../../services/api';
import moment from 'moment';

const Recommendations = () => {
  const [loading, setLoading] = useState(false);
  const [competitionRecommendations, setCompetitionRecommendations] = useState([]);
  const [teamRecommendations, setTeamRecommendations] = useState([]);
  const [activeTab, setActiveTab] = useState('competitions');
  const [error, setError] = useState(null);

  useEffect(() => {
    loadRecommendations();
  }, []);

  const loadRecommendations = async () => {
    setLoading(true);
    setError(null);
    
    try {
      console.log('开始加载推荐数据...');
      
      const [competitions, teams] = await Promise.all([
        recommendationAPI.getCompetitionRecommendations(10).then(data => {
          console.log('竞赛推荐API原始响应:', data);
          console.log('竞赛推荐数据类型:', typeof data);
          console.log('竞赛推荐是否为数组:', Array.isArray(data));
          if (Array.isArray(data)) {
            console.log('竞赛推荐数组长度:', data.length);
            console.log('竞赛推荐第一项:', data[0]);
          }
          return data;
        }).catch(err => {
          console.error('获取竞赛推荐失败:', err);
          return [];
        }),
        
        recommendationAPI.getTeamRecommendations(10).then(data => {
          console.log('队伍推荐API原始响应:', data);
          console.log('队伍推荐数据类型:', typeof data);
          console.log('队伍推荐是否为数组:', Array.isArray(data));
          if (Array.isArray(data)) {
            console.log('队伍推荐数组长度:', data.length);
            console.log('队伍推荐第一项:', data[0]);
          }
          return data;
        }).catch(err => {
          console.error('获取队伍推荐失败:', err);
          return [];
        })
      ]);
      
      console.log('处理后的竞赛推荐:', competitions);
      console.log('处理后的队伍推荐:', teams);
      
      const competitionsArray = Array.isArray(competitions) ? competitions : [];
      const teamsArray = Array.isArray(teams) ? teams : [];
      
      console.log('最终设置的竞赛推荐数组长度:', competitionsArray.length);
      console.log('最终设置的队伍推荐数组长度:', teamsArray.length);
      
      setCompetitionRecommendations(competitionsArray);
      setTeamRecommendations(teamsArray);
      
    } catch (error) {
      console.error('加载推荐数据失败:', error);
      setError('加载推荐数据失败，请稍后重试');
      message.error('加载推荐数据失败');
    } finally {
      setLoading(false);
    }
  };

  const renderCompetitionItem = (recommendation) => {
    // 安全检查
    if (!recommendation || !recommendation.item) {
      return null;
    }

    const competition = recommendation.item;
    const score = recommendation.score || 0;
    const explanation = recommendation.explanation || '基于您的兴趣和技能推荐';

    return (
      <List.Item
        actions={[
          <Button type="primary" size="small" key="detail">
            查看详情
          </Button>,
          <Button size="small" key="favorite">
            收藏
          </Button>
        ]}
      >
        <List.Item.Meta
          avatar={<TrophyOutlined style={{ fontSize: '24px', color: '#1890ff' }} />}
          title={
            <div>
              <span>{competition.name || '未知竞赛'}</span>
              <Rate
                disabled
                value={score * 5}
                style={{ marginLeft: '12px', fontSize: '14px' }}
              />
              <span style={{ marginLeft: '8px', color: '#666' }}>
                匹配度: {(score * 100).toFixed(1)}%
              </span>
            </div>
          }
          description={
            <div>
              <p>{competition.description || '暂无描述'}</p>
              <div style={{ marginTop: '8px' }}>
                <Tag color="blue">{competition.category || '未知分类'}</Tag>
                <Tag color="green">{competition.level || '未知级别'}</Tag>
                {competition.registrationDeadline && (
                  <Tag color="orange">
                    截止: {moment(competition.registrationDeadline).format('YYYY-MM-DD')}
                  </Tag>
                )}
                {competition.organizer && (
                  <Tag color="purple">
                    主办: {competition.organizer}
                  </Tag>
                )}
              </div>
              <div style={{ marginTop: '8px', color: '#1890ff' }}>
                推荐理由: {explanation}
              </div>
            </div>
          }
        />
      </List.Item>
    );
  };

  const renderTeamItem = (recommendation) => {
    // 安全检查
    if (!recommendation || !recommendation.item) {
      return null;
    }

    const team = recommendation.item;
    const score = recommendation.score || 0;
    const explanation = recommendation.explanation || '基于您的技能匹配推荐';

    return (
      <List.Item
        actions={[
          <Button
            type="primary"
            size="small"
            key="apply"
            disabled={team.status !== 'RECRUITING'}
          >
            {team.status === 'RECRUITING' ? '申请加入' : '已满员'}
          </Button>,
          <Button size="small" key="detail">
            查看详情
          </Button>
        ]}
      >
        <List.Item.Meta
          avatar={<TeamOutlined style={{ fontSize: '24px', color: '#52c41a' }} />}
          title={
            <div>
              <span>{team.name || '未知队伍'}</span>
              <Rate
                disabled
                value={score * 5}
                style={{ marginLeft: '12px', fontSize: '14px' }}
              />
              <span style={{ marginLeft: '8px', color: '#666' }}>
                匹配度: {(score * 100).toFixed(1)}%
              </span>
            </div>
          }
          description={
            <div>
              <p>{team.description || '暂无描述'}</p>
              <div style={{ marginTop: '8px' }}>
                {team.competition?.name && (
                  <Tag color="purple">{team.competition.name}</Tag>
                )}
                <Tag color="cyan">
                  {team.currentMembers || 0}/{team.maxMembers || 0} 人
                </Tag>
                <Tag color={getTeamStatusColor(team.status)}>
                  {getTeamStatusText(team.status)}
                </Tag>
                {team.leader && (
                  <Tag color="gold">
                    队长: {team.leader.realName || team.leader.username || '未知'}
                  </Tag>
                )}
              </div>
              <div style={{ marginTop: '8px', color: '#52c41a' }}>
                推荐理由: {explanation}
              </div>
            </div>
          }
        />
      </List.Item>
    );
  };

  // 获取队伍状态颜色
  const getTeamStatusColor = (status) => {
    switch (status) {
      case 'RECRUITING': return 'green';
      case 'FULL': return 'orange';
      case 'DISBANDED': return 'red';
      default: return 'default';
    }
  };

  // 获取队伍状态文本
  const getTeamStatusText = (status) => {
    switch (status) {
      case 'RECRUITING': return '招募中';
      case 'FULL': return '已满员';
      case 'DISBANDED': return '已解散';
      default: return '未知状态';
    }
  };

  const competitionsTab = (
    <Card>
      {error ? (
        <Alert
          message="加载失败"
          description={error}
          type="error"
          showIcon
          action={
            <Button size="small" onClick={loadRecommendations}>
              重试
            </Button>
          }
        />
      ) : competitionRecommendations.length > 0 ? (
        <List
          loading={loading}
          dataSource={competitionRecommendations}
          renderItem={renderCompetitionItem}
          pagination={{
            pageSize: 5,
            showSizeChanger: false,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      ) : (
        <Empty
          description={loading ? "加载中..." : "暂无推荐的竞赛"}
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        >
          {!loading && (
            <Button type="primary" onClick={loadRecommendations}>
              刷新推荐
            </Button>
          )}
        </Empty>
      )}
    </Card>
  );

  const teamsTab = (
    <Card>
      {error ? (
        <Alert
          message="加载失败"
          description={error}
          type="error"
          showIcon
          action={
            <Button size="small" onClick={loadRecommendations}>
              重试
            </Button>
          }
        />
      ) : teamRecommendations.length > 0 ? (
        <List
          loading={loading}
          dataSource={teamRecommendations}
          renderItem={renderTeamItem}
          pagination={{
            pageSize: 5,
            showSizeChanger: false,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      ) : (
        <Empty
          description={loading ? "加载中..." : "暂无推荐的队伍"}
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        >
          {!loading && (
            <Button type="primary" onClick={loadRecommendations}>
              刷新推荐
            </Button>
          )}
        </Empty>
      )}
    </Card>
  );

  const tabItems = [
    {
      key: 'competitions',
      label: (
        <span>
          <TrophyOutlined />
          竞赛推荐 ({competitionRecommendations.length})
        </span>
      ),
      children: competitionsTab,
    },
    {
      key: 'teams',
      label: (
        <span>
          <TeamOutlined />
          队伍推荐 ({teamRecommendations.length})
        </span>
      ),
      children: teamsTab,
    },
  ];

  return (
    <div>
      <Card
        title="智能推荐"
        extra={
          <Button
            onClick={loadRecommendations}
            loading={loading}
            type="primary"
          >
            {loading ? '加载中...' : '刷新推荐'}
          </Button>
        }
        style={{ marginBottom: '16px' }}
      >
        <div style={{ marginBottom: '16px' }}>
          <p style={{ margin: 0, color: '#666' }}>
            基于您的技能特长和兴趣偏好，为您推荐合适的竞赛和队伍
          </p>
        </div>

        {/* 推荐统计 */}
        <div style={{
          display: 'flex',
          gap: '24px',
          padding: '16px',
          background: '#f5f5f5',
          borderRadius: '8px',
          marginBottom: '16px'
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
              {competitionRecommendations.length}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>竞赛推荐</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#52c41a' }}>
              {teamRecommendations.length}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>队伍推荐</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#faad14' }}>
              {competitionRecommendations.length + teamRecommendations.length}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>总推荐数</div>
          </div>
        </div>
      </Card>

      <Spin spinning={loading}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
          size="large"
        />
      </Spin>
    </div>
  );
};

export default Recommendations;