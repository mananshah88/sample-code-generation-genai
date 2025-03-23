function fn() {
    var env = karate.env; // get system property 'karate.env'
    karate.log('karate.env system property was:', env);
    if (!env) {
        env = 'dev';
    }
    var config = {
        baseUrl: 'http://localhost:8080'
    };
    if (env === 'dev') {
        // customize
        config.baseUrl = 'http://localhost:8080';
    } else if (env === 'qa') {
        // customize
        config.baseUrl = 'http://qa-server:8080';
    }
    return config;
}
