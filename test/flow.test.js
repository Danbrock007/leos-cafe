'use strict';
const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),os=require('node:os'),path=require('node:path');
const dir=fs.mkdtempSync(path.join(os.tmpdir(),'leos-test-'));
process.env.DATA_FILE=path.join(dir,'store.json');process.env.ADMIN_USER='testadmin';process.env.ADMIN_PASSWORD='testpass123';process.env.ADMIN_PHONE='03000000000';process.env.NODE_ENV='test';
const {server}=require('../server');
let base,admin='',customer='',rider='';
async function call(url,method='GET',data,cookie=''){let r=await fetch(base+'/api'+url,{method,headers:{'Content-Type':'application/json',cookie},body:data?JSON.stringify(data):undefined}),d=await r.json();return {status:r.status,data:d,cookie:r.headers.get('set-cookie')?.split(';')[0]||''};}
async function otp(number){let logs=[],old=console.log;console.log=(...args)=>logs.push(args.join(' '));try{let r=await call('/otp/request','POST',{phone:number});assert.equal(r.status,200);}finally{console.log=old}return logs.join(' ').match(/\b\d{6}\b/)?.[0];}
test('order, rider dispatch, rewards, access checks, cash reporting',async()=>{await new Promise(resolve=>server.listen(0,resolve));base=`http://127.0.0.1:${server.address().port}`;try{
 let a=await call('/login','POST',{identity:'testadmin',password:'testpass123'});assert.equal(a.status,200);admin=a.cookie;
 let code=await otp('03001234567');assert.ok(code);let c=await call('/register','POST',{name:'Test Customer',phone:'03001234567',code,password:'customer123',address:'Street 1 Islamabad'});assert.equal(c.status,201);customer=c.cookie;
 code=await otp('03007654321');let r=await call('/register','POST',{name:'Test Rider',phone:'03007654321',code,password:'riderpass123',accountType:'rider'});assert.equal(r.status,201);rider=r.cookie;
 let users=(await call('/admin/users','GET',null,admin)).data.users, riderId=users.find(x=>x.phone==='03007654321').id;assert.equal((await call('/admin/users/'+riderId,'PATCH',{approve:true},customer)).status,403);assert.equal((await call('/admin/users/'+riderId,'PATCH',{approve:true},admin)).status,200);
 let product=(await call('/public')).data.menu.find(x=>x.category==='Pizza');let settings=await call('/admin/settings','PATCH',{open:'00:00',close:'23:59',acceptingOrders:true},admin);assert.equal(settings.status,200);
 let first;for(let i=0;i<10;i++){let placed=await call('/orders','POST',{items:[{id:product.id,qty:1}],address:'Street 1 Islamabad'},customer);assert.equal(placed.status,201);let id=placed.data.order.id;if(i===0)first=id;for(let status of ['confirmed','preparing','ready'])assert.equal((await call('/orders/'+id+'/status','PATCH',{status},admin)).status,200);assert.equal((await call('/orders/'+id+'/status','PATCH',{status:'out_for_delivery'},rider)).status,200);assert.equal((await call('/orders/'+id+'/location','PATCH',{latitude:33.7,longitude:73.1},rider)).status,200);assert.equal((await call('/orders/'+id+'/status','PATCH',{status:'delivered'},rider)).status,200);}
 let me=await call('/me','GET',null,customer);assert.equal(me.data.user.points,50);assert.equal(me.data.user.completedOrders,10);
 let report=await call('/admin/report','GET',null,admin);assert.equal(report.data.delivered,10);assert.equal(report.data.cash,product.price*10);assert.equal((await call('/admin/report','GET',null,customer)).status,403);
 let unauthorized=await call('/orders/'+first+'/location','PATCH',{latitude:33,longitude:73},customer);assert.equal(unauthorized.status,403);
 let item=await call('/admin/menu/'+product.id,'PATCH',{name:product.name,category:'Pizza',price:600,available:false},admin);assert.equal(item.status,200);assert.ok(!(await call('/public')).data.menu.some(x=>x.id===product.id));assert.ok((await call('/admin/menu','GET',null,admin)).data.menu.some(x=>x.id===product.id));
}finally{await new Promise(resolve=>server.close(resolve));fs.rmSync(dir,{recursive:true,force:true});}});
