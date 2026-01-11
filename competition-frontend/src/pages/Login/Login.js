import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Tabs } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { userAPI } from '../../services/api';
import './Login.css';

const Login = () => {
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('login');
    const navigate = useNavigate();

    const handleLogin = async (values) => {
        setLoading(true);
        try {
            const response = await userAPI.login({
                username: values.username,
                password: values.password
            });

            localStorage.setItem('token', response.token);
            message.success('登录成功');
            navigate('/');
        } catch (error) {
            message.error(error.response?.data?.message || '登录失败');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (values) => {
        setLoading(true);
        try {
            await userAPI.register(values);
            message.success('注册成功，请登录');
            setActiveTab('login');
        } catch (error) {
            message.error(error.response?.data?.message || '注册失败');
        } finally {
            setLoading(false);
        }
    };

    const loginForm = (
        <Form
            name="login"
            onFinish={handleLogin}
            autoComplete="off"
            size="large"
        >
            <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
            >
                <Input
                    prefix={<UserOutlined />}
                    placeholder="用户名"
                />
            </Form.Item>

            <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
            >
                <Input.Password
                    prefix={<LockOutlined />}
                    placeholder="密码"
                />
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    loading={loading}
                    style={{ width: '100%' }}
                >
                    登录
                </Button>
            </Form.Item>
        </Form>
    );

    const registerForm = (
        <Form
            name="register"
            onFinish={handleRegister}
            autoComplete="off"
            size="large"
        >
            <Form.Item
                name="username"
                rules={[
                    { required: true, message: '请输入用户名' },
                    { min: 3, max: 50, message: '用户名长度必须在3-50之间' }
                ]}
            >
                <Input
                    prefix={<UserOutlined />}
                    placeholder="用户名"
                />
            </Form.Item>

            <Form.Item
                name="email"
                rules={[
                    { required: true, message: '请输入邮箱' },
                    { type: 'email', message: '邮箱格式不正确' }
                ]}
            >
                <Input
                    prefix={<MailOutlined />}
                    placeholder="邮箱"
                />
            </Form.Item>

            <Form.Item
                name="password"
                rules={[
                    { required: true, message: '请输入密码' },
                    { min: 6, message: '密码长度不能少于6位' }
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined />}
                    placeholder="密码"
                />
            </Form.Item>

            <Form.Item
                name="confirmPassword"
                dependencies={['password']}
                rules={[
                    { required: true, message: '请确认密码' },
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (!value || getFieldValue('password') === value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('两次输入的密码不一致'));
                        },
                    }),
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined />}
                    placeholder="确认密码"
                />
            </Form.Item>

            <Form.Item
                name="realName"
                rules={[{ required: true, message: '请输入真实姓名' }]}
            >
                <Input
                    prefix={<UserOutlined />}
                    placeholder="真实姓名"
                />
            </Form.Item>

            <Form.Item
                name="school"
                rules={[{ required: true, message: '请输入学校' }]}
            >
                <Input placeholder="学校" />
            </Form.Item>

            <Form.Item name="major">
                <Input placeholder="专业" />
            </Form.Item>

            <Form.Item name="grade">
                <Input placeholder="年级" />
            </Form.Item>

            <Form.Item name="phone">
                <Input
                    prefix={<PhoneOutlined />}
                    placeholder="手机号"
                />
            </Form.Item>

            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    loading={loading}
                    style={{ width: '100%' }}
                >
                    注册
                </Button>
            </Form.Item>
        </Form>
    );

    const tabItems = [
        {
            key: 'login',
            label: '登录',
            children: loginForm,
        },
        {
            key: 'register',
            label: '注册',
            children: registerForm,
        },
    ];

    return (
        <div className="login-container">
            <Card
                title="学科竞赛管理平台"
                className="login-card"
            >
                <Tabs
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    items={tabItems}
                    centered
                />
            </Card>
        </div>
    );
};

export default Login;