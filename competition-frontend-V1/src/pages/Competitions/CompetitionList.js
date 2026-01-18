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
  Pagination 
} from 'antd';
import { 
  SearchOutlined, 
  TrophyOutlined, 
  CalendarOutlined,
  TeamOutlined 
} from '@ant-design/icons';
import { competitionAPI } from '../../services/api';
import moment from 'moment';

const { Search } = Input;
const { Option } = Select;

const CompetitionList = () => {
  const [loading, setLoading] = useState(false);
  const [competitions, setCompetitions] = useState([]);
  const [filteredCompetitions, setFilteredCompetitions] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [filters, setFilters] = useState({
    category: '',
    level: '',
    status: '',
    keyword: ''
  });

  useEffect(() => {
    loadCompetitions();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [competitions, filters]);

  const loadCompetitions = async () => {
    setLoading(true);
    try {
      const data = await competitionAPI.getCompetitions({
        page: 0,
        size: 100 // 加载更多数据用于前端过滤
      });
      setCompetitions(data.content || []);
    } catch (error) {
      message.error('加载竞赛列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...competitions];

    // 关键词搜索
    if (filters.keyword) {
      filtered = filtered.filter(comp => 
        comp.name.toLowerCase().includes(filters.keyword.toLowerCase()) ||
        comp.description?.toLowerCase().includes(filters.keyword.toLowerCase())
      );
    }

    // 分类过滤
    if (filters.category) {
      filtered = filtered.filter(comp => comp.category === filters.category);
    }

    // 级别过滤
    if (filters.level) {
      filtered = filtered.filter(comp => comp.level === filters.level);
    }

    // 状态过滤
    if (filters.status) {
      filtered = filtered.filter(comp => comp.status === filters.status);
    }

    setFilteredCompetitions(filtered);
    setCurrentPage(1);
  };

  const handleSearch = (value) => {
    setFilters(prev => ({ ...prev, keyword: value }));
  };

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'UPCOMING': return 'blue';
      case 'ONGOING': return 'green';
      case 'FINISHED': return 'gray';
      default: return 'default';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'UPCOMING': return '即将开始';
      case 'ONGOING': return '进行中';
      case 'FINISHED': return '已结束';
      default: return status;
    }
  };

  const renderCompetitionItem = (competition) => (
    <List.Item
      actions={[
        <Button type="primary" size="small">
          查看详情
        </Button>,
        <Button size="small">
          收藏
        </Button>
      ]}
    >
      <List.Item.Meta
        avatar={<TrophyOutlined style={{ fontSize: '32px', color: '#1890ff' }} />}
        title={
          <div>
            <span style={{ fontSize: '16px', fontWeight: 'bold' }}>
              {competition.name}
            </span>
            <Tag 
              color={getStatusColor(competition.status)} 
              style={{ marginLeft: '12px' }}
            >
              {getStatusText(competition.status)}
            </Tag>
          </div>
        }
        description={
          <div>
            <p style={{ margin: '8px 0', color: '#666' }}>
              {competition.description}
            </p>
            <Space wrap style={{ marginTop: '12px' }}>
              <Tag icon={<TrophyOutlined />} color="blue">
                {competition.category}
              </Tag>
              <Tag color="green">
                {competition.level}
              </Tag>
              <Tag icon={<TeamOutlined />} color="orange">
                最大队伍规模: {competition.maxTeamSize}人
              </Tag>
              <Tag icon={<CalendarOutlined />} color="purple">
                报名截止: {moment(competition.registrationDeadline).format('YYYY-MM-DD')}
              </Tag>
            </Space>
            <div style={{ marginTop: '8px', fontSize: '12px', color: '#999' }}>
              主办方: {competition.organizer} | 
              比赛时间: {moment(competition.startDate).format('YYYY-MM-DD')} 至 {moment(competition.endDate).format('YYYY-MM-DD')}
            </div>
          </div>
        }
      />
    </List.Item>
  );

  const paginatedData = filteredCompetitions.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  return (
    <div>
      <Card style={{ marginBottom: '16px' }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Search
            placeholder="搜索竞赛名称或描述"
            allowClear
            enterButton={<SearchOutlined />}
            size="large"
            onSearch={handleSearch}
          />
          
          <Space wrap>
            <Select
              placeholder="选择分类"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => handleFilterChange('category', value)}
            >
              <Option value="编程">编程</Option>
              <Option value="数学建模">数学建模</Option>
              <Option value="创新创业">创新创业</Option>
              <Option value="设计">设计</Option>
              <Option value="其他">其他</Option>
            </Select>
            
            <Select
              placeholder="选择级别"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => handleFilterChange('level', value)}
            >
              <Option value="国际级">国际级</Option>
              <Option value="国家级">国家级</Option>
              <Option value="省级">省级</Option>
              <Option value="校级">校级</Option>
            </Select>
            
            <Select
              placeholder="选择状态"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => handleFilterChange('status', value)}
            >
              <Option value="UPCOMING">即将开始</Option>
              <Option value="ONGOING">进行中</Option>
              <Option value="FINISHED">已结束</Option>
            </Select>
            
            <Button onClick={loadCompetitions}>
              刷新
            </Button>
          </Space>
        </Space>
      </Card>

      <Card 
        title={`竞赛列表 (${filteredCompetitions.length})`}
        extra={
          <Button type="primary">
            发布竞赛
          </Button>
        }
      >
        <List
          loading={loading}
          dataSource={paginatedData}
          renderItem={renderCompetitionItem}
          locale={{ emptyText: '暂无竞赛数据' }}
        />
        
        {filteredCompetitions.length > pageSize && (
          <div style={{ textAlign: 'center', marginTop: '16px' }}>
            <Pagination
              current={currentPage}
              total={filteredCompetitions.length}
              pageSize={pageSize}
              onChange={setCurrentPage}
              showSizeChanger={false}
              showQuickJumper
              showTotal={(total, range) => 
                `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
              }
            />
          </div>
        )}
      </Card>
    </div>
  );
};

export default CompetitionList;