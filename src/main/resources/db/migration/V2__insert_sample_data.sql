INSERT INTO departments (name, description) VALUES
    ('IT',         'Software development and internal IT support'),
    ('HR',         'Recruitment, onboarding and employee relations'),
    ('Finance',    'Accounting, payroll and budgeting'),
    ('DevOps',     'CI/CD pipelines, cloud infrastructure and monitoring'),
    ('Operations', 'Day-to-day business operations and facilities');

INSERT INTO employees (employee_code, first_name, last_name, email, phone, department_id, job_title, joining_date, salary, status) VALUES
    ('EMP001', 'Anita',  'Sharma', 'anita.sharma@example.com',  '+91 98765 43210', (SELECT id FROM departments WHERE name = 'IT'),         'Software Engineer',   '2022-04-11', 85000.00, 'ACTIVE'),
    ('EMP002', 'Rahul',  'Verma',  'rahul.verma@example.com',   '+91 98765 43211', (SELECT id FROM departments WHERE name = 'DevOps'),     'DevOps Engineer',     '2023-01-16', 92000.00, 'ACTIVE'),
    ('EMP003', 'Priya',  'Nair',   'priya.nair@example.com',    '+91 98765 43212', (SELECT id FROM departments WHERE name = 'HR'),         'HR Manager',          '2021-07-01', 78000.00, 'ACTIVE'),
    ('EMP004', 'Karan',  'Mehta',  'karan.mehta@example.com',   '+91 98765 43213', (SELECT id FROM departments WHERE name = 'Finance'),    'Financial Analyst',   '2022-10-03', 70000.00, 'ACTIVE'),
    ('EMP005', 'Sneha',  'Reddy',  'sneha.reddy@example.com',   '+91 98765 43214', (SELECT id FROM departments WHERE name = 'Operations'), 'Operations Lead',     '2020-02-17', 88000.00, 'INACTIVE'),
    ('EMP006', 'Arjun',  'Patel',  'arjun.patel@example.com',   '+91 98765 43215', (SELECT id FROM departments WHERE name = 'DevOps'),     'Site Reliability Engineer', '2024-06-03', 95000.00, 'ACTIVE');
