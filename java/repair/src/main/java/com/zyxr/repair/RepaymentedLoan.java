package com.zyxr.repair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.ibatis.session.SqlSession;

import com.mysql.jdbc.Connection;
import com.zyxr.feecalculator.Repayment;
import com.zyxr.feecalculator.RepaymentSchedule;
import com.zyxr.repair.dao.DbManager;
import com.zyxr.repair.model.AssetInfo;
import com.zyxr.repair.model.DispatchInfo;
import com.zyxr.repair.model.LoanRepayment;
import com.zyxr.repair.model.Loans;
import com.zyxr.repair.model.MissionLoans;
import com.zyxr.repair.utils.Tool;

//已还的还款表修复
public class RepaymentedLoan {
	public List<DispatchInfo> dispatchs;
	
	public RepaymentedLoan() {
		dispatchs=new ArrayList<DispatchInfo>();
	}

	public void doJob() {
		String url="jdbc:mysql://192.168.51.145:3306/product";
		String jdbcDriver="com.mysql.jdbc.Driver";
		String user="search";
		String password="search@zyxr.com";
		
		DbUtils.loadDriver(jdbcDriver);
		Connection conn=null;
		
		try {
			conn=(Connection) DriverManager.getConnection(url,user,password);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("连接MySQL失败,url="+url);
			Tool.exitSystem();
		}
		
		String sql="select asset_id,asset_name,borrower_uid,borrower_name,total_amount,"
				+ "annual_rate,add_rate,phase_total,phase_mode,repay_mode,"
				+ "full_time "
				+ "from product.t_assets where asset_id=%d";
		QueryRunner qr=new QueryRunner();
		ResultSetHandler<List<AssetInfo>> handler=new BeanListHandler<AssetInfo>(AssetInfo.class);
		List<AssetInfo> assets=null;
		try {
			assets = qr.query(conn, sql, handler);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL查询错误,sql="+sql);
			Tool.exitSystem();
		}
		
		//System.out.println(loans);
		
//		Tool.initUidMapFromLoans(assets);
		
		for (AssetInfo asset:assets) {
			long capital=asset.total_amount;
			long annualRate=asset.annual_rate;
			long periods=asset.phase_total;
			long periodType=asset.phase_mode;
			String valueDate="2016-09-21 00:00:00";
			int repaymentMode=asset.repay_mode;
			RepaymentSchedule rs=new RepaymentSchedule(capital,annualRate,periods,periodType,valueDate);
			rs.calRepaymentSchedule(repaymentMode);
			List<Repayment> repayments=rs.getRepaymentList();
			checkRepayments(conn,asset,repayments);
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("关闭MySQL连接失败");
			Tool.exitSystem();
		}
		
		saveFile();
	}
	
	public void checkRepayments(Connection conn,AssetInfo loan,List<Repayment> repayments) {
/*		QueryRunner qr=new QueryRunner();
		String sql=String.format("select repayment_id,asset_id,state,phase,repay_mode,"
				+ "expect_principal,expect_interest "
				+ "from product.t_repayment_%02d where asset_id=%s",
				loan.asset_id);
		ResultSetHandler<List<LoanRepayment>> handler=new BeanListHandler<LoanRepayment>(LoanRepayment.class);
		List<LoanRepayment> missionRepayments=null;
		try {
			missionRepayments = qr.query(conn, sql, handler);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL查询错误,sql="+sql);
			Tool.exitSystem();
		}

		for (Repayment repayment:repayments) {
			for (LoanRepayment missionRepayment:missionRepayments) {
				if (missionRepayment.isRepaymented() || repayment.getPeriodNo() != missionRepayment.period) {
					continue;
				}
				
				DispatchInfo dispatch=new DispatchInfo();
				long diffCapital=repayment.getCapital() - Tool.getMoney(missionRepayment.stillPrincipal);
				long diffInterest=repayment.getInterest() - Tool.getMoney(missionRepayment.stillInterest);
				
				if (diffCapital>10) {
					System.out.println("本金相差太大错误,diffCapital="+diffCapital);
					System.out.println("missionLoan="+loan);
					System.out.println("repayment="+repayment);
					System.out.println("missionRepayment="+missionRepayment);
					Tool.exitSystem();
				}
				if (diffInterest>10) {
					System.out.println("利息相差太大错误,diffInterest="+diffInterest);
					System.out.println("missionLoan="+loan);
					System.out.println("repayment="+repayment);
					System.out.println("missionRepayment="+missionRepayment);
					Tool.exitSystem();
				}

				long newBorrowerUid=loan.borrower_uid;
				long newAssetId=loan.asset_id;
				long phase=repayment.getPeriodNo();
				
				
				//标旧id
				dispatch.oldAssetId=loan.asset_id;
				//标题
				dispatch.title=loan.asset_name;
				//借款本金
				dispatch.amount=loan.borrowamount;
				//年化利率
				dispatch.annualRate=loan.annualrate;
				//加息利率
				dispatch.annualAddRate=loan.add_rate;
				//还款方式
				dispatch.repayMode=Tool.getRepaymentMode(loan.repaymode);
				//期数类型
				dispatch.phaseType=Tool.getPhaseType(loan.periodmode);
				//借款总期数
				dispatch.phaseTotal=loan.periods;
				//标新id
				dispatch.newAssetId=newAssetId;
				//借款人姓名
				dispatch.borrowerName=loan.borrowername;
				//借款人旧uid
				dispatch.oldBorrowerUid=loan.borrower;
				//借款人新uid
				dispatch.newBorrowerUid=Tool.getNewUidByOldUid(loan.borrower);
				//还款期号
				dispatch.phase=phase;
				//旧本金
				dispatch.oldPrincipal=missionRepayment.stillPrincipal;
				//新本金
				dispatch.newPrincipal=repayment.getCapital();
				//本金差额
				dispatch.diffPrincipal=diffCapital;
				//旧利息
				dispatch.oldInterest=missionRepayment.stillInterest;
				//新利息
				dispatch.newInterest=repayment.getInterest();
				//利息差额
				dispatch.diffInterest=diffInterest;
				//总差额
				dispatch.diffTotal=diffCapital+diffInterest;
				
				if (Tool.isNewRepayed(dispatch)) {
					continue;
				}
				
				if (dispatch.diffTotal > 0) {
					dispatchs.add(dispatch);
				}
				break;
			}
		}*/
	}
	
	public void saveFile() {
		//标旧id,标题,借款本金,年化利率,加息利率,还款方式,期数类型,借款期数,标新id,借款人姓名,借款人旧uid,借款人新uid,还款期号,旧本金,新本金,本金差额,旧利息,新利息,利息差额,总差额
		StringBuffer fileDispatch=new StringBuffer();
		fileDispatch.append("标旧id,标题,借款本金,年化利率,加息利率,还款方式,期数类型,借款期数,标新id,借款人姓名,借款人旧uid,借款人新uid,还款期号,旧本金,新本金,本金差额,旧利息,新利息,利息差额,总差额");
		fileDispatch.append("\n");
		
		StringBuffer sumFileDispatch=new StringBuffer();
		sumFileDispatch.append("借款人姓名,借款人旧uid,借款人新uid,总差额");
		sumFileDispatch.append("\n");

		StringBuffer fileFixPrincipalSqls=new StringBuffer();
		StringBuffer fileFixInterestSqls=new StringBuffer();
		
		Map<String, DispatchInfo> sumDispatch=new HashMap<String, DispatchInfo>();
		
		for (DispatchInfo dispatch:dispatchs) {

			if (dispatch.diffPrincipal>0) {
				String fixSql=String.format("update product.t_repayments set expect_principal=%d+%d where asset_id=%d and borrower_uid=%d and phase=%d and state=0 and expect_principal=%d;",
						Tool.getMoney(dispatch.oldPrincipal),
						dispatch.diffPrincipal,
						dispatch.newAssetId,
						dispatch.newBorrowerUid,
						dispatch.phase,
						Tool.getMoney(dispatch.oldPrincipal));
				fileFixPrincipalSqls.append(fixSql);
				fileFixPrincipalSqls.append("\n");
				
				fixSql=String.format("update product.t_repayment_%02d set expect_principal=%d+%d where asset_id=%s and borrower_uid=%s and phase=%d and state=0 and expect_principal=%d;",
						dispatch.newBorrowerUid%100,
						Tool.getMoney(dispatch.oldPrincipal),
						dispatch.diffPrincipal,
						dispatch.newAssetId,
						dispatch.newBorrowerUid,
						dispatch.phase,
						Tool.getMoney(dispatch.oldPrincipal));
				fileFixPrincipalSqls.append(fixSql);
				fileFixPrincipalSqls.append("\n");
			}
		
			if (dispatch.diffInterest>0) {
				String fixSql=String.format("update product.t_repayments set expect_interest=%d+%d where asset_id=%d and borrower_uid=%d and phase=%d and state=0 and expect_interest=%d;",
						Tool.getMoney(dispatch.oldInterest),
						dispatch.diffInterest,
						dispatch.newAssetId,
						dispatch.newBorrowerUid,
						dispatch.phase,
						Tool.getMoney(dispatch.oldInterest));
				fileFixInterestSqls.append(fixSql);
				fileFixInterestSqls.append("\n");
				
				fixSql=String.format("update product.t_repayment_%02d set expect_interest=%d+%d where asset_id=%s and borrower_uid=%s and phase=%d and state=0 and expect_interest=%d;",
						dispatch.newBorrowerUid%100,
						Tool.getMoney(dispatch.oldInterest),
						dispatch.diffInterest,
						dispatch.newAssetId,
						dispatch.newBorrowerUid,
						dispatch.phase,
						Tool.getMoney(dispatch.oldInterest));
				fileFixInterestSqls.append(fixSql);
				fileFixInterestSqls.append("\n");
			}
			
			if (dispatch.diffTotal > 0) {
				fileDispatch.append(dispatch.getOut());
				fileDispatch.append("\n");
				
				DispatchInfo dispatchInfo=sumDispatch.get(String.valueOf(dispatch.newBorrowerUid));
				if (null==dispatchInfo) {
					dispatchInfo=dispatch;
					sumDispatch.put(String.valueOf(dispatch.newBorrowerUid), dispatch);
				} else {
					dispatchInfo.diffTotal += dispatch.diffTotal;
				}
			}

		}
		
		
		try {
			if (fileDispatch.length() > 0) {
				File w=new File("D:\\job_projects\\python\\asset_dispatch_money.dat");
				   FileWriter fw = new FileWriter(w.getAbsoluteFile());
				   BufferedWriter bw = new BufferedWriter(fw);
				   bw.write(fileDispatch.toString());
				   bw.close();			
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("保存文件失败-散标还款修复，D:\\job_projects\\python\\asset_dispatch_money.dat");
			Tool.exitSystem();
		}

		Iterator<Map.Entry<String, DispatchInfo>> iterator2=sumDispatch.entrySet().iterator();
		while (iterator2.hasNext()) {
			Map.Entry<String, DispatchInfo> entry=iterator2.next();
			DispatchInfo dispatchInfo=entry.getValue();
			sumFileDispatch.append(dispatchInfo.getSumOut());
			sumFileDispatch.append("\n");
		}

		try {
			if (sumFileDispatch.length() > 0) {
				File w=new File("D:\\job_projects\\python\\asset_sum_dispatch_money.dat");
				   FileWriter fw = new FileWriter(w.getAbsoluteFile());
				   BufferedWriter bw = new BufferedWriter(fw);
				   bw.write(sumFileDispatch.toString());
				   bw.close();			
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("保存文件失败-散标还款修复，D:\\job_projects\\python\\asset_sum_dispatch_money.dat");
			Tool.exitSystem();
		}

		try {
			if (fileFixPrincipalSqls.length() > 0) {
				File w=new File("D:\\job_projects\\python\\asset_fix_principal_sqls.dat");
				   FileWriter fw = new FileWriter(w.getAbsoluteFile());
				   BufferedWriter bw = new BufferedWriter(fw);
				   bw.write(fileFixPrincipalSqls.toString());
				   bw.close();			
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("保存文件失败-散标还款修复，D:\\job_projects\\python\\asset_fix_principal_sqls.dat");
			Tool.exitSystem();
		}

		try {
			if (fileFixInterestSqls.length() > 0) {
				File w=new File("D:\\job_projects\\python\\asset_fix_interest_sqls.dat");
				   FileWriter fw = new FileWriter(w.getAbsoluteFile());
				   BufferedWriter bw = new BufferedWriter(fw);
				   bw.write(fileFixInterestSqls.toString());
				   bw.close();			
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("保存文件失败-散标还款修复，D:\\job_projects\\python\\asset_fix_interest_sqls.dat");
			Tool.exitSystem();
		}
	}
	
	public void testMyBaits() {
        SqlSession session = DbManager.getSqlFactory().openSession();
        String statement = "com.zyxr.repair.missionLoansMap.getMissionLoans";
        List<MissionLoans> loans=session.selectList(statement);
        for (MissionLoans loan:loans) {
        	System.out.println(loan);
        }
        System.out.println(loans.size());
	}

}
