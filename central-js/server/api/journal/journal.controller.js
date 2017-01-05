var request = require('request');
var syncSubject = require('../subject/subject.service');

function getOptions(req) {
  var config = require('../../config/environment');
  //var config = require('../../config');

  var activiti = config.activiti;

  return {
    protocol: activiti.protocol,
    hostname: activiti.hostname,
    port: activiti.port,
    path: activiti.path,
    username: activiti.username,
    password: activiti.password
  };
}

module.exports.getHistoryEvents = function (req, res) {

  var options = getOptions(req);
  var url = options.protocol
    + '://'
    + options.hostname
    + options.path
    + '/action/event/getHistoryEvents';

  var callback = function (error, response, body) {
    res.send(body);
    res.end();
  };

  let hostnameAuth;
  if (req.headers.host.search('localhost') != -1) {
    hostnameAuth = 'accounts.kitsoft.kiev.ua';
  } else if (req.headers.host.search('central.es') != -1) {
    hostnameAuth = 'accounts.kyivcity.gov.ua';
    // hostnameAuth = 'accounts.kitsoft.kiev.ua';
  } else if (req.headers.host.search('test3.es') != -1) {
    hostnameAuth = 'accounts.kyivcity.gov.ua';
    // hostnameAuth = 'accounts.kitsoft.kiev.ua';
  } else if (req.headers.host == "es.kievcity.gov.ua") {
    hostnameAuth = 'accounts.kyivcity.gov.ua';
  } else {
    hostnameAuth = 'accounts.kyivcity.gov.ua';
  }

  if (!req.query.access_token) {
    return request.get({
      'url': url,
      'auth': {
        'username': options.username,
        'password': options.password
      },
      'qs': {
        'nID_Subject': req.session.subject.nID,
        'nID_HistoryEvent_Service': req.params.nID_HistoryEvent_Service,
        'bGrouped': req.params.nID_HistoryEvent_Service ? false : true
      }
    }, callback);
  } else {
    var getUserId = ()=> {
      return new Promise((resolve, reject)=> {
        request(`https://${hostnameAuth}/user/info?access_token=${req.query.access_token}`, {json: true}, (error, response, data)=> {
          if (response.statusCode == 401) {
            return reject(data)
          }
          ;
          resolve(data)
        })
      })
    }

    var getMyUser = (body)=> {
      var inn = undefined;
      if (body.user.services) {
        for (let key in body.user.services) {
          if (typeof inn == 'undefined' && (typeof body.user.services[key].inn != "undefined" || typeof body.user.services[key].edrpoucode != "undefined"))
            inn = body.user.services[key].inn || body.user.services[key].edrpoucode;
        }
      }
      if (inn == undefined) {
        return reject({errorCode: 401, message: "User haven`t personal code"});
      }

      return new Promise((resolve, reject)=> {
        syncSubject.sync(inn, function (error, response, data) {
          resolve(data)
        })
      })
    }

    var getHistory = (data)=> {
      return request.get({
        'url': url,
        'auth': {
          'username': options.username,
          'password': options.password
        },
        'qs': {
          'nID_Subject': data.nID,
          'nID_HistoryEvent_Service': req.params.nID_HistoryEvent_Service,
          'bGrouped': req.params.nID_HistoryEvent_Service ? false : true
        }
      }, callback);
    }

    getUserId().then(getMyUser).then(getHistory).catch((err)=> {
      res.status(401).send(err);
    })
  }

};

module.exports.setHistoryEvent = function (req, res) {

  var options = getOptions(req);
  var url = options.protocol
    + '://'
    + options.hostname
    + options.path
    + '/action/event/setHistoryEvent';

  var callback = function (error, response, body) {
    // console.log(body);
    res.send(body);
    res.end();
  };

//    oHistoryEvent:{
//        "nID": 445
//            ,"nID_Subject": 2
//            ,"nID_HistoryEventType":3
//            ,"sEventName":"" - консолидированное название события
//            ,"sMessage":""
//            ,"sDate":"2015-11-25 23:23:59.325"
//    }

  return request.post({
    'url': url,
    'auth': {
      'username': options.username,
      'password': options.password
    },
    'qs': {
      "nID": 445,
      "nID_Subject": 2,
      "nID_HistoryEventType": 3,
      "sEventName": "",
      "sMessage": "",
      "sDate": "2015-11-25 23:23:59.325"
    }
  }, callback);
};
