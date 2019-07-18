const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const api = require('./api');
const cors = require('cors');


// Middlewares
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));
app.use(cors({
    origin: ['http://211.249.62.8:8080'],
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    credentials: true // enable set cookie
}));
/*
app.use(function (req, res, next) { //보안상의 이류로 서버는 기본적으로 같은 서버가 아닌 다른 곳에서 오는 요청을 차단한다. 또한 클라이언트에서 오는 요청도 마찬가지다. 하지만 API는 클라이언트를 위한 프로그램이므로 다른 곳에서 오는 요청을 허가해야 한다. 이것을 HTTP 접근 제어 또는 CORS(Cross-origin resource sharing)라고한다.
  res.header('Access-Control-Allow-Origin','*'); //요청이 허용되는 url을 route을 제외하고 적는다 *는 모든 요청을 허용한다.
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE'); //요청이 허용되는 HTTP verb 목록을 적는다. *를 사용할 수 없다.
  res.header('Access-Control-Allow-Headers', 'content-type'); //요청이 허용되는 HTTP header 목록을 적는다. *를 사용할 수 없다.
  next();
});
*/

// API
app.use('/api', api);

app.listen(3000, () => console.log("server listening..."));