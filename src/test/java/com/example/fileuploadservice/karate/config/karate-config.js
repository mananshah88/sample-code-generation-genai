function fn() {
    var env = karate.env || 'local';
    var config = {
        baseUrl: 'http://localhost:8081',
        apiPath: '/api/files'
    };
    
    if (env === 'local') {
        config.baseUrl = 'http://localhost:8081';
    } else if (env === 'dev') {
        config.baseUrl = 'http://dev-api.example.com';
    } else if (env === 'prod') {
        config.baseUrl = 'http://api.example.com';
    }
    
    karate.log('Environment:', env);
    karate.log('Base URL:', config.baseUrl);
    
    return config;
} 