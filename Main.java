import java.util.*;
import java.io.*;
/*
算法流程:
	1.从文件中得到初始被关注集合
	2.从此集合中选择符合支持度的项集合
	3.从此项集合中选择一个项作为前缀,然后试图找拥有此前缀的更高级别频繁项集
	4.直至遍历所有可能
	5.对已找到的频繁项集排序输出

notice:
	1.记录的是被关注列表而非关注列表;
		不必再额外开一个表记录每个人的被关注数量了;
	2.将频繁项集转换为string再保存;
		list无法比较大小,就无法排序,去重,转成string则可利用自带的比较器;

*/
public class Main {
	public static void main(String[] args) {
		itemset.print("----import begin----");//导入数据
		long start_time = System.currentTimeMillis();
		
		candi_set info=file_io.readfiles();
		
		itemset.print("time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- import end ----");
		
		
		
		
		itemset.minsup=(int) (info.second.size()*0.01);//设置最小支持度(此处的info.second.size()并非实际的事项数量,仅作为近似值)
		/*
		 * 最小支持度过小会极大增加时间消耗,亦可能内存溢出,请当心
		 * */
		itemset.print("----find frequent itemset begin----");//寻找频繁项集
		start_time = System.currentTimeMillis();
		itemset.print("min-support is "+itemset.minsup);
		List<String> next_list=itemset.get_next_list(info);//得到候选集合
		itemset.next_round(info,next_list);//通过矩阵和候选集合递归寻找频繁项集
		
		itemset.print("time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- find frequent itemset end ----");
		
		
		
		
		
		itemset.print("----output itemset begin----");
		start_time = System.currentTimeMillis();
		file_io.output_itemsets();//输出到文件
	//	itemset.print_itemsets();//输出到控制台
		itemset.print("get "+itemset.final_ans.size()+" itemsets, time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- output itemset end ----");
		
		
		
	}
}





class itemset{//全局变量及方法
	public static Integer minsup;//最小支持度,
	public static String dir = "C:\\Users\\Jin\\Desktop\\233\\apriori\\twitter\\twitter";//数据文件夹位置
	public static Set<ans_set> final_ans=new TreeSet<ans_set>();//存储频繁项集的容器

	/*
	next_round
	从new_list中选择一个基础项A作为本级别递归搜索的频繁项集前缀,
	对于每一个A;
		对应的缩小被关注集合tmp,产生副本ans;
		调用get_next_list获得新项集ans中的频繁项集,记录于final_ans,并返回一个副本B;
		使用ans和B进行更深层次的递归;
		直至B为空,说明本次尝试不会有更高级别的频繁项集了,返回至上一层递归;
	*/
	public static void next_round( candi_set tmp , List<String> new_list){
		
		candi_set ans;
		for(int i=0;i<new_list.size();++i){//若候选集合为空,则不会继续寻找
			String new_item=new_list.get(i);
			ans= new candi_set(tmp.first+" "+new_item,new HashMap<String,Set<String>>());//递归越深,tmp.first即频繁项集中已确定的元素越多
			
			for(int j=i+1;j<new_list.size();++j){
				String n=new_list.get(j);
				Set<String> tmp_set=ans.second.get(n);
				if(tmp_set==null)
					tmp_set=new HashSet<String>();
				for(String m:tmp.second.get(new_item)){
					/*
					 * 三层循环:从候选集中选择一个作为下一轮的确定项;
					 * 然后从候选集中找另一个候选项m
					 * 若有任意元素m(不一定是候选项),既关注了new_item,又关注了m,则说明new_item和m有机会成为更长的频繁项集
					 * 则把m和n记录在一个新表tmp_set中
					 * */
					if(tmp.second.get(n).contains(m)){
						tmp_set.add(m);
					}
				}
				ans.second.put(n,tmp_set);
			}
			List<String> next_list=get_next_list(ans);
			/*
			 * tmp_set制作完成后,本函数将其中满足条件的项集保存到final_ans,准备最后输出
			 * 并返回一个新的候选集
			 * */
			next_round(ans,next_list);//携带新候选集与新表进行下一次递归
		}
		
	}
	/*
	get_next_list
	从tmp中得到新的频繁项,与tmp的前缀合并,得到新的频繁项集;
		因为递归是有序的,而频繁项集是无序的,因此将频繁项集排序后转化成字符串,使用TreeSet进行去重,同时排序;
		返回一份频繁项集的副本,用以缩小被关注集合;
	*/
	public static List<String> get_next_list(candi_set tmp){
		List<String> ans=new ArrayList<String>();
		for(Map.Entry<String, Set<String>> i:tmp.second.entrySet()){
			if(i.getValue().size()<itemset.minsup)//如果某个项不符合最低支持度,不保存它
				continue;
			
			/*
			 * 将频繁项集排序并合成一个字符串,以便排序输出
			 *
			 * */
			String pre_arr[]=(tmp.first.trim()+" "+i.getKey()).trim().split(" ");
			Arrays.sort(pre_arr);
			String tmp_ans=pre_arr[0];
			for(int j=1;j<pre_arr.length;++j){
				tmp_ans+=" "+pre_arr[j];
			}
			final_ans.add(new ans_set(tmp_ans,i.getValue().size()));
			ans.add(i.getKey());//获得新的候选集
		}
		return ans;
	}
	/*
	懒得写那么长的输出语句...所以造了个语法糖;
	*/
	public static void print(String a){
		System.out.println(a);
	}
	/*
	输出答案到屏幕
	*/
	public static void print_itemsets(){//输出频繁集合到屏幕
			for(ans_set i:itemset.final_ans){
				print("["+i.str+"] support: "+i.sup);
			}
	}
}



class file_io{
	//读写文件
	
	/*
	根据指定的文件夹
		读取文件夹下所有的后缀为edges的文件
		对于每个文件
			读取每一行
			将关系不重复的添加到被关注集合
	*/
	public static candi_set readfiles(){
		int cont=0;
		candi_set all_map=new candi_set("",new HashMap<String,Set<String>>());
		File root = new File(itemset.dir);
		File[] files = root.listFiles();
		FileReader fr =null;
		BufferedReader br = null;
		for(File file:files){
			if(file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("edges")==false)//如果不是edges文件则跳过
				continue;
			String id=file.getName().substring(0,file.getName().length()-6);//获得文件名中的userid
			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				String[] arr_str;
				String tmp;
				/*
				 * 此处记录的是被谁关注,而不是关注了谁
				 * 这样记的优点在于可以快速的找出哪一项的支持度是满足最小支持度的
				 * 
				 * */
				while ((tmp= br.readLine()) != null) {
					arr_str=tmp.split(" ");
					Set<String> fst_set=all_map.second.get(arr_str[0]);
					if(fst_set==null)
						fst_set=new HashSet<String>();
					Set<String> snd_set=all_map.second.get(arr_str[1]);
					if(snd_set==null)
						snd_set=new HashSet<String>();
					
					
					if(!snd_set.contains(id)){
						snd_set.add(id);
						++cont;
					}
					
					if(!snd_set.contains(arr_str[0])){
						snd_set.add(arr_str[0]);
						++cont;
					}
					if(!fst_set.contains(id)){
						fst_set.add(id);
						++cont;
					}
					
					all_map.second.put(arr_str[0], fst_set);
					all_map.second.put(arr_str[1],snd_set);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	try {
			    br.close();
			    fr.close();
			}catch (IOException e) {
			    e.printStackTrace();
			}
		}
		
		itemset.print("import "+cont+" edges");
		return all_map;
	}
	/*
	将答案写到给定文件夹下的frequent_itemset.ext下;
	输出格式与输出到屏幕的函数相同
	*/
	public static void output_itemsets(){//输出频繁项集到文件
		
		try {
			File out_file = new File(itemset.dir+"\\frequent_itemset.txt");
			out_file.createNewFile();
			FileWriter out_writer = new FileWriter(out_file);
			BufferedWriter out_buf = new BufferedWriter(out_writer);
			for(ans_set i:itemset.final_ans){
				out_buf.write("["+i.str+"] support: "+i.sup+"\n");
			}
			out_buf.flush();
			
			out_buf.close();
			out_writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/*
便于去重和排序频繁项集所用的对象,重写了比较函数
*/
class ans_set implements Comparable<ans_set>{
	String str;
	Integer sup;   
    public ans_set(String a,Integer b){
    	str=a;
    	sup=b;
    }

	@Override
	public int compareTo(ans_set o) {
		int a=str.split(" ").length;
		int b=o.str.split(" ").length;
		if(a!=b)
			return a-b;
		return str.compareTo(o.str);
	}
}


/*
便于传递被关注集合和记录其对应前缀的对象
*/
class candi_set{

    String first;//被关注集合对应的前缀
    Map<String,Set<String>> second;//一个被关注集合;
    public candi_set() {
    	first = null;second = null;
    }
    public candi_set(String f,Map<String,Set<String>> s){
        first = f;
        second = s;
    }
}



