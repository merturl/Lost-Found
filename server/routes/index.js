var express = require('express');
var router = express.Router();

var admin = require('firebase-admin');
var FCM = require('fcm-node');
var serviceAccount = require('../memowebapp-a4b04-firebase-adminsdk-9occh-3c772a50b7');

var firebaseAdmin = admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://memowebapp-a4b04.firebaseio.com"
});


var serverKey = 'AAAAEm0j3B4:APA91bEI6_j8z5zezd2ymdKSKL7jZq_tjxldkz_Edl0Y8AbOs5Rf_ZKRtmsoJfINPT1rdySqsRGqsMM6iKkrq4z9ni-KmQ2HZzv2i8cKXWM6dooUM67IIlp1yxzCmxO1j0mjUX0EErUN';

/** 안드로이드 단말에서 추출한 token값 */
// 안드로이드 App이 적절한 구현절차를 통해서 생성해야 하는 값이다.
// 안드로이드 단말에서 Node server로 POST방식 전송 후,
// Node서버는 이 값을 DB에 보관하고 있으면 된다.

/** 발송할 Push 메시지 내용 */


/** 아래는 푸시메시지 발송절차 */
var db = firebaseAdmin.database();
var refmsg = db.ref("message");
var reftoken = db.ref("tokens");
refmsg.on("value", function(snapshot) {
  snapshot.forEach(msgs=>{
    msgs.forEach(msg=>{
      if(!msg.val().send){
        // console.log(msgs.key+"/"+msg.key);
        reftoken.child(msg.val().to).once("value", function(snapshot) {
          firebaseAdmin.auth().getUser(msg.val().from).then(user=>{
            var push_data = {
              // 수신대상
              to: snapshot.val(),
              // App이 실행중이지 않을 때 상태바 알림으로 등록할 내용
              // 메시지 중요도
              priority: "high",
              
              // App 패키지 이름
              restricted_package_name: "com.example.jongho.newproject_1",
              // App에게 전달할 데이터
              data: {
                  title: user.email,
                  body: msg.val().msg
              }
            };
            var fcm = new FCM(serverKey);
            fcm.send(push_data, function(err, response) {
              if (err) {
                  console.log(msgs.key+"/"+msg.key);
                  console.error('Push메시지 발송에 실패했습니다.');
                  console.error(err);
                  return;
            }
            db.ref(`message/${msgs.key}/${msg.key}`).update({send: true});
            console.log(response);
          });
        }); 
      });
    }
  });
});
});
// ref.on("value", function(snap) {
//   console.log("initial data loaded!", snap.val);
// });
// function pushFCM(){
//   firebaseAdmin.database().ref(`memos`).child("8OisT7Cs3uTCazSw5l2aA1l5pvQ2").on('value', function(snapshot) {
//     console.log(snapshot.val());
//     console.log("123123123");
//     if(snapshot.exists()){
//       console.log(snapshot.val());
//     }
//   });
// }

/* GET home page. */
router.get('/', function(req, res, next) {  
  console.log(id, weight);
  res.json([
    {
      id: id
    }
  ]);
  if(res){
    id++;
  }
  
  
});

router.get('/motor/:address', function(req, res, next) {
  var motor;
  firebaseAdmin.database().ref(`motor/${req.params.address}`).once('value', function(snapshot) {
    if(snapshot.exists()){
      console.log(snapshot.exists())
      motor = snapshot.val().isRotation;
      console.log(motor);
      res.json([
        {
          motor: motor
        }
      ]);
    }else{
      console.log(snapshot.exists())
      res.json([
        {
          motor: 0
        }
      ]);
    }
  }).then(()=>{
    firebaseAdmin.database().ref(`motor/${req.params.address}`).set({isRotation: 0});
  });
  // if(motor == 1){
  //   motor = 0;
  // }else {
  //   motor = 1;
  // }
  // firebaseAdmin.database().ref('weight/00:30:f9:13:c3:de').child(new Date().toStrin  g()).set({
  //   weight: motor
  // });
});

router.get('/weight/:weight/:address', function(req, res, next) {
  var date = new Date();
  console.log(req.params.weight);
  // firebaseAdmin.database().ref(`weight/${req.params.address}/${Date.now()}`).set({weight: Number(req.params.weight)});

  //set now weight
  var number = 0;
  //set now date  
  
  firebaseAdmin.database().ref(`nowweight/${req.params.address}`).once('value', snapshot=>{
    if(snapshot.exists()){
      console.log(snapshot.exists())
      if(req.params.weight < snapshot.val().weight){
        firebaseAdmin.database().ref(`weight/${req.params.address}/${date.getFullYear()}${date.getMonth()+1}${date.getDate()-number}/${date.getHours()}${date.getMinutes()}${date.getMilliseconds()}`)
        .set({weight: Number( snapshot.val().weight-req.params.weight)});
      }else{
        firebaseAdmin.database().ref(`weight/${req.params.address}/${date.getFullYear()}${date.getMonth()+1}${date.getDate()-number}/${date.getHours()}${date.getMinutes()}${date.getMilliseconds()}`)
        .set({weight: Number(0)});
      }
    }
  }).then(()=>{firebaseAdmin.database().ref(`nowweight/${req.params.address}`).set({weight: Number(req.params.weight)})});
  //get weight over date
  
  firebaseAdmin.database().ref(`weight/${req.params.address}/${date.getFullYear()}${date.getMonth()+1}${date.getDate()-number}`).once('value', snapshot=>{
    if(snapshot.exists()){
      console.log(snapshot.exists())
      var sum = 0;
      snapshot.forEach(day=>{
        sum += day.val().weight;
      });
      //set weight in db
      firebaseAdmin.database().ref(`averageweight/${req.params.address}/${date.getFullYear()}${date.getMonth()+1}${date.getDate()-number}`).set({weight: sum});
    }
  });
  res.send();
});

router.post('/weight', function(req, res, next){
  let weight = req.body.weight;
  id++;
  console.log("Post : "+ req.body.weight + ", id : " + id);
  // res.json([
  //   {
  //     motor: motor
  //   }
  // ]);
  
});

  
router.post('/home', function(req, res, next) {
  var weight = req.body.weight;
  console.log(weight);
  res.send();
});

module.exports = router;
