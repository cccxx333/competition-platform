import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  message,
  Tabs,
  Select,
  Rate,
  Tag,
  Space,
  Divider,
  List,
  Modal,
  Spin
} from 'antd';
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  BankOutlined,
  BookOutlined,
  PlusOutlined,
  DeleteOutlined,
  EditOutlined
} from '@ant-design/icons';
import { userAPI, skillAPI } from '../../services/api';

const { TextArea } = Input;
const { Option } = Select;

const Profile = () => {
  const [loading, setLoading] = useState(false);
  const [skillsLoading, setSkillsLoading] = useState(false);
  const [form] = Form.useForm();
  const [skillForm] = Form.useForm();
  
  // 用户信息状态
  const [userInfo, setUserInfo] = useState({});
  
  // 技能相关状态
  const [allSkills, setAllSkills] = useState([]); // 所有可选技能
  const [skillCategories, setSkillCategories] = useState([]); // 技能分类
  const [userSkills, setUserSkills] = useState([]); // 用户已有技能
  const [selectedCategory, setSelectedCategory] = useState(''); // 选中的技能分类
  const [filteredSkills, setFilteredSkills] = useState([]); // 过滤后的技能
  
  // 模态框状态
  const [skillModalVisible, setSkillModalVisible] = useState(false);
  const [editingSkill, setEditingSkill] = useState(null);

  useEffect(() => {
    loadUserProfile();
    loadSkillsData();
  }, []);

  // 加载用户信息
  const loadUserProfile = async () => {
    setLoading(true);
    try {
      const userData = await userAPI.getProfile();
      console.log('用户信息:', userData);
      setUserInfo(userData);
      
      // 填充表单
      form.setFieldsValue({
        username: userData.username,
        email: userData.email,
        realName: userData.realName,
        school: userData.school,
        major: userData.major,
        grade: userData.grade,
        phone: userData.phone
      });
      
      // 加载用户技能
      if (userData.id) {
        await loadUserSkills(userData.id);
      }
      
    } catch (error) {
      console.error('加载用户信息失败:', error);
      message.error('加载用户信息失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载技能数据
  const loadSkillsData = async () => {
    setSkillsLoading(true);
    try {
      const [skillsData, categoriesData] = await Promise.all([
        skillAPI.getAllSkills(),
        skillAPI.getAllCategories()
      ]);
      
      console.log('所有技能:', skillsData);
      console.log('技能分类:', categoriesData);
      
      setAllSkills(skillsData || []);
      setSkillCategories(categoriesData || []);
      setFilteredSkills(skillsData || []);
      
    } catch (error) {
      console.error('加载技能数据失败:', error);
      message.error('加载技能数据失败');
      setAllSkills([]);
      setSkillCategories([]);
    } finally {
      setSkillsLoading(false);
    }
  };

  // 加载用户技能
  const loadUserSkills = async (userId) => {
    try {
      const userSkillsData = await userAPI.getUserSkills(userId);
      console.log('用户技能:', userSkillsData);
      setUserSkills(userSkillsData || []);
    } catch (error) {
      console.error('加载用户技能失败:', error);
      setUserSkills([]);
    }
  };

  // 更新用户基本信息
  const handleUpdateProfile = async (values) => {
    setLoading(true);
    try {
      await userAPI.updateProfile(values);
      message.success('个人信息更新成功');
      setUserInfo({ ...userInfo, ...values });
    } catch (error) {
      console.error('更新个人信息失败:', error);
      message.error('更新失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 技能分类变化处理
  const handleCategoryChange = (category) => {
    setSelectedCategory(category);
    if (category) {
      const filtered = allSkills.filter(skill => skill.category === category);
      setFilteredSkills(filtered);
    } else {
      setFilteredSkills(allSkills);
    }
  };

  // 打开添加/编辑技能模态框
  const openSkillModal = (skill = null) => {
    setEditingSkill(skill);
    setSkillModalVisible(true);
    
    if (skill) {
      // 编辑模式
      skillForm.setFieldsValue({
        skillId: skill.skillId,
        proficiency: skill.proficiency
      });
      // 设置对应的分类
      const skillInfo = allSkills.find(s => s.id === skill.skillId);
      if (skillInfo) {
        setSelectedCategory(skillInfo.category);
        handleCategoryChange(skillInfo.category);
      }
    } else {
      // 新增模式
      skillForm.resetFields();
      setSelectedCategory('');
      setFilteredSkills(allSkills);
    }
  };

  // 关闭技能模态框
  const closeSkillModal = () => {
    setSkillModalVisible(false);
    setEditingSkill(null);
    skillForm.resetFields();
    setSelectedCategory('');
    setFilteredSkills(allSkills);
  };

  // 保存技能
  const handleSaveSkill = async (values) => {
    try {
      const skillData = {
        skillId: values.skillId,
        proficiency: values.proficiency
      };

      if (editingSkill) {
        // 更新技能
        await userAPI.updateUserSkill(editingSkill.id, skillData);
        message.success('技能更新成功');
      } else {
        // 添加技能
        await userAPI.addUserSkill(skillData);
        message.success('技能添加成功');
      }

      // 重新加载用户技能
      await loadUserSkills(userInfo.id);
      closeSkillModal();
      
    } catch (error) {
      console.error('保存技能失败:', error);
      message.error('保存失败，请重试');
    }
  };

  // 删除技能
  const handleDeleteSkill = async (skillId) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个技能吗？',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await userAPI.deleteUserSkill(skillId);
          message.success('技能删除成功');
          await loadUserSkills(userInfo.id);
        } catch (error) {
          console.error('删除技能失败:', error);
          message.error('删除失败，请重试');
        }
      }
    });
  };

  // 获取技能名称
  const getSkillName = (skillId) => {
    const skill = allSkills.find(s => s.id === skillId);
    return skill ? skill.name : '未知技能';
  };

  // 获取技能分类
  const getSkillCategory = (skillId) => {
    const skill = allSkills.find(s => s.id === skillId);
    return skill ? skill.category : '未知分类';
  };

  // 获取熟练度文本
  const getProficiencyText = (proficiency) => {
    const levels = ['', '初学者', '入门', '熟练', '精通', '专家'];
    return levels[proficiency] || '未知';
  };

  // 获取熟练度颜色
  const getProficiencyColor = (proficiency) => {
    const colors = ['', 'red', 'orange', 'yellow', 'blue', 'green'];
    return colors[proficiency] || 'default';
  };

  const tabItems = [
    {
      key: 'basic',
      label: '基本信息',
      children: (
        <Card>
          <Form
            form={form}
            layout="vertical"
            onFinish={handleUpdateProfile}
          >
            <Form.Item
              label="用户名"
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input 
                prefix={<UserOutlined />} 
                disabled // 用户名通常不允许修改
              />
            </Form.Item>

            <Form.Item
              label="邮箱"
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' }
              ]}
            >
              <Input prefix={<MailOutlined />} />
            </Form.Item>

            <Form.Item
              label="真实姓名"
              name="realName"
              rules={[{ required: true, message: '请输入真实姓名' }]}
            >
              <Input prefix={<UserOutlined />} />
            </Form.Item>

            <Form.Item
              label="学校"
              name="school"
              rules={[{ required: true, message: '请输入学校' }]}
            >
              <Input prefix={<BankOutlined />} />
            </Form.Item>

            <Form.Item
              label="专业"
              name="major"
              rules={[{ required: true, message: '请输入专业' }]}
            >
              <Input prefix={<BookOutlined />} />
            </Form.Item>

            <Form.Item
              label="年级"
              name="grade"
              rules={[{ required: true, message: '请选择年级' }]}
            >
              <Select placeholder="请选择年级">
                <Option value="大一">大一</Option>
                <Option value="大二">大二</Option>
                <Option value="大三">大三</Option>
                <Option value="大四">大四</Option>
                <Option value="研一">研一</Option>
                <Option value="研二">研二</Option>
                <Option value="研三">研三</Option>
                <Option value="博士">博士</Option>
                <Option value="教师">教师</Option>
              </Select>
            </Form.Item>

            <Form.Item
              label="手机号"
              name="phone"
              rules={[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
              ]}
            >
              <Input prefix={<PhoneOutlined />} />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                更新信息
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'skills',
      label: '技能管理',
      children: (
        <div>
          <Card 
            title="我的技能" 
            extra={
              <Button 
                type="primary" 
                icon={<PlusOutlined />}
                onClick={() => openSkillModal()}
                disabled={skillsLoading}
              >
                添加技能
              </Button>
            }
          >
            <Spin spinning={skillsLoading}>
              {userSkills.length > 0 ? (
                <List
                  dataSource={userSkills}
                  renderItem={userSkill => (
                    <List.Item
                      actions={[
                        <Button 
                          type="link" 
                          icon={<EditOutlined />}
                          onClick={() => openSkillModal(userSkill)}
                        >
                          编辑
                        </Button>,
                        <Button 
                          type="link" 
                          danger
                          icon={<DeleteOutlined />}
                          onClick={() => handleDeleteSkill(userSkill.id)}
                        >
                          删除
                        </Button>
                      ]}
                    >
                      <List.Item.Meta
                        title={
                          <Space>
                            <span>{getSkillName(userSkill.skillId)}</span>
                            <Tag color={getProficiencyColor(userSkill.proficiency)}>
                              {getProficiencyText(userSkill.proficiency)}
                            </Tag>
                          </Space>
                        }
                        description={
                          <div>
                            <Tag color="blue">{getSkillCategory(userSkill.skillId)}</Tag>
                            <Rate 
                              disabled 
                              value={userSkill.proficiency} 
                              style={{ fontSize: '14px', marginLeft: '8px' }}
                            />
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              ) : (
                <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
                  <BookOutlined style={{ fontSize: '48px', marginBottom: '16px' }} />
                  <div>还没有添加任何技能</div>
                  <Button 
                    type="primary" 
                    style={{ marginTop: '16px' }}
                    onClick={() => openSkillModal()}
                  >
                    添加第一个技能
                  </Button>
                </div>
              )}
            </Spin>
          </Card>

          {/* 技能添加/编辑模态框 */}
          <Modal
            title={editingSkill ? '编辑技能' : '添加技能'}
            open={skillModalVisible}
            onCancel={closeSkillModal}
            footer={null}
            width={600}
          >
            <Form
              form={skillForm}
              layout="vertical"
              onFinish={handleSaveSkill}
            >
              <Form.Item
                label="技能分类"
                required
              >
                <Select
                  placeholder="请选择技能分类"
                  value={selectedCategory}
                  onChange={handleCategoryChange}
                  loading={skillsLoading}
                >
                  <Option value="">全部分类</Option>
                  {skillCategories.map(category => (
                    <Option key={category} value={category}>
                      {category}
                    </Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                label="技能"
                name="skillId"
                rules={[{ required: true, message: '请选择技能' }]}
              >
                <Select
                  placeholder="请选择技能"
                  showSearch
                  filterOption={(input, option) =>
                    option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                  loading={skillsLoading}
                >
                  {filteredSkills.map(skill => (
                    <Option key={skill.id} value={skill.id}>
                      <div>
                        <div>{skill.name}</div>
                        <div style={{ fontSize: '12px', color: '#999' }}>
                          {skill.description}
                        </div>
                      </div>
                    </Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                label="熟练度"
                name="proficiency"
                rules={[{ required: true, message: '请选择熟练度' }]}
              >
                <div>
                  <Rate style={{ marginBottom: '8px' }} />
                  <div style={{ fontSize: '12px', color: '#666' }}>
                    1星-初学者，2星-入门，3星-熟练，4星-精通，5星-专家
                  </div>
                </div>
              </Form.Item>

              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit">
                    {editingSkill ? '更新' : '添加'}
                  </Button>
                  <Button onClick={closeSkillModal}>
                    取消
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Modal>
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Tabs items={tabItems} />
    </div>
  );
};

export default Profile;