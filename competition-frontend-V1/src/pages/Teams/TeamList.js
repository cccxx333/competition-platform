import React, { useState, useEffect } from 'react';
import { 
  Card, 
  List, 
  Button, 
  Input, 
  Select, 
  Tag, 
  Space,
  message,
  Avatar,
  Tooltip
} from 'antd';
import { 
  SearchOutlined, 
  TeamOutlined, 
  UserOutlined,
  PlusOutlined,
  CrownOutlined
} from '@ant-design/icons';
import { teamAPI } from '../../services/api';

const { Search } = Input;
const { Option } = Select;

const TeamList = () => {
  const [loading, setLoading] = useState(false);
  const [teams, setTeams] = useState([]);
  const [filteredTeams, setFilteredTeams] = useState([]);
  const [filters, setFilters] = useState({
    status: '',
    keyword: ''
  });

  useEffect(() => {
    loadTeams();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [teams, filters]);

  const loadTeams = async () => {
    setLoading(true);
    try {
      const data = await teamAPI.getTeams({ page: 0, size: 100 });
      setTeams(data.content || []);
    } catch (error) {
      message.error('加载队伍列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...teams];

    if (filters.keyword) {
      filtered = filtered.filter(team => 
        team.name.toLowerCase().includes(filters.keyword.toLowerCase()) ||
        team.description?.toLowerCase().includes(filters.keyword.toLowerCase())
      );
    }

    if (filters.status) {
      filtered = filtered.filter(team => team.status === filters.status);
    }

    setFilteredTeams(filtered);
  };

  const handleSearch = (value) => {
    setFilters(prev => ({ ...prev, keyword: value }));
  };

  const handleJoinTeam = async (teamId) => {
    try {
      await teamAPI.joinTeam(teamId);
      message.success('申请加入队伍成功');
      loadTeams();
    } catch (error) {
      message.error('申请失败');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'RECRUITING': return 'green';
      case 'FULL': return 'orange';
      case 'DISBANDED': return 'red';
      default: return 'default';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'RECRUITING': return '招募中';
      case 'FULL': return '已满员';
      case 'DISBANDED': return '已解散';
      default: return status;
    }
  };

  const renderTeamItem = (team) => (
    <List.Item
      actions={[
        team.status === 'RECRUITING' ? (
          <Button 
            type="primary" 
            size="small"
            onClick={() => handleJoinTeam(team.id)}
          >
            申请加入
          </Button>
        ) : (
          <Button size="small" disabled>
            {getStatusText(team.status)}
          </Button>
        ),
        <Button size="small">
          查看详情
        </Button>
      ]}
    >
      <List.Item.Meta
        avatar={<TeamOutlined style={{ fontSize: '32px', color: '#52c41a' }} />}
        title={
          <div>
            <span style={{ fontSize: '16px', fontWeight: 'bold' }}>
              {team.name}
            </span>
            <Tag 
              color={getStatusColor(team.status)} 
              style={{ marginLeft: '12px' }}
            >
              {getStatusText(team.status)}
            </Tag>
          </div>
        }
        description={
          <div>
            <p style={{ margin: '8px 0', color: '#666' }}>
              {team.description}
            </p>
            <Space wrap style={{ marginTop: '12px' }}>
              <Tag color="blue">
                {team.competition?.name}
              </Tag>
              <Tag color="purple">
                <UserOutlined /> {team.currentMembers}/{team.maxMembers} 人
              </Tag>
              <Tag color="gold">
                <CrownOutlined /> 队长: {team.leader?.realName}
              </Tag>
            </Space>
            
            {/* 显示队伍成员头像 */}
            {team.teamMembers && team.teamMembers.length > 0 && (
              <div style={{ marginTop: '8px' }}>
                <span style={{ fontSize: '12px', color: '#999', marginRight: '8px' }}>
                  成员:
                </span>
                <Avatar.Group maxCount={4} size="small">
                  {team.teamMembers.map(member => (
                    <Tooltip key={member.id} title={member.user.realName}>
                      <Avatar 
                        src={member.user.avatarUrl}
                        icon={<UserOutlined />}
                      />
                    </Tooltip>
                  ))}
                </Avatar.Group>
              </div>
            )}
          </div>
        }
      />
    </List.Item>
  );

  return (
    <div>
      <Card style={{ marginBottom: '16px' }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Search
            placeholder="搜索队伍名称或描述"
            allowClear
            enterButton={<SearchOutlined />}
            size="large"
            onSearch={handleSearch}
          />
          
          <Space wrap>
            <Select
              placeholder="选择状态"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
            >
              <Option value="RECRUITING">招募中</Option>
              <Option value="FULL">已满员</Option>
              <Option value="DISBANDED">已解散</Option>
            </Select>
            
            <Button onClick={loadTeams}>
              刷新
            </Button>
          </Space>
        </Space>
      </Card>

      <Card 
        title={`队伍列表 (${filteredTeams.length})`}
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            创建队伍
          </Button>
        }
        >
        <List
          loading={loading}
          dataSource={filteredTeams}
          renderItem={renderTeamItem}
          locale={{ emptyText: '暂无队伍数据' }}
          pagination={{
            pageSize: 10,
            showSizeChanger: false,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>
    </div>
  );
};

export default TeamList;