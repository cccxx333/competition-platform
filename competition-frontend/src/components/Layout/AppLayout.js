import React, { useState, useEffect } from 'react';
import { Layout, Menu, Avatar, Dropdown, message } from 'antd';
import { 
  HomeOutlined, 
  TrophyOutlined, 
  TeamOutlined, 
  UserOutlined,
  LogoutOutlined,
  BellOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { userAPI } from '../../services/api';

const { Header, Sider, Content } = Layout;

const AppLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    loadUserInfo();
  }, []);

  const loadUserInfo = async () => {
    try {
      const user = await userAPI.getProfile();
      setUserInfo(user);
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    message.success('退出登录成功');
    navigate('/login');
  };

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
    },
    {
      key: '/competitions',
      icon: <TrophyOutlined />,
      label: '竞赛管理',
    },
    {
      key: '/teams',
      icon: <TeamOutlined />,
      label: '队伍管理',
    },
    {
      key: '/recommendations',
      icon: <BellOutlined />,
      label: '智能推荐',
    },
    {
      key: '/profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
  ];

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        collapsible 
        collapsed={collapsed} 
        onCollapse={setCollapsed}
        theme="dark"
      >
        <div style={{ 
          height: '32px', 
          margin: '16px', 
          background: 'rgba(255, 255, 255, 0.3)',
          borderRadius: '6px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontWeight: 'bold'
        }}>
          {collapsed ? 'CP' : '竞赛平台'}
        </div>
        <Menu
          theme="dark"
          selectedKeys={[location.pathname]}
          mode="inline"
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      
      <Layout>
        <Header style={{ 
          padding: '0 16px', 
          background: '#fff',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <div />
          <Dropdown
            menu={{ items: userMenuItems }}
            placement="bottomRight"
          >
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              cursor: 'pointer',
              padding: '0 12px',
              borderRadius: '6px',
              transition: 'background-color 0.3s'
            }}>
              <Avatar 
                src={userInfo?.avatarUrl} 
                icon={<UserOutlined />}
                style={{ marginRight: '8px' }}
              />
              <span>{userInfo?.realName || userInfo?.username || '用户'}</span>
            </div>
          </Dropdown>
        </Header>
        
        <Content style={{ 
          margin: '16px',
          padding: '24px',
          background: '#fff',
          borderRadius: '8px',
          minHeight: 'calc(100vh - 112px)'
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;