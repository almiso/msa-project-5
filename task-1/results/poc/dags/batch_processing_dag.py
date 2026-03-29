from datetime import datetime, timedelta
import random

from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.bash import BashOperator
from airflow.operators.empty import EmptyOperator

default_args = {
    'owner': 'marketing_dev',
    'depends_on_past': False,
    'email': ['alerts@company.local'],
    'email_on_failure': True,
    'email_on_retry': False,
    'retries': 2,
    'retry_delay': timedelta(seconds=10),
}

# Simulates batch read and data quality calculation
def simulate_data_read(**kwargs):
    quality_score = random.randint(1, 10)
    print(f"Data batch read complete. Quality score: {quality_score}/10")
    return quality_score


# Branching based on quality score from previous step
def decide_branch(**kwargs):
    ti = kwargs['ti']
    score = ti.xcom_pull(task_ids='read_data_source')
    
    if score >= 5:
        return 'process_high_quality_data'
    return 'handle_low_quality_data'


with DAG(
    'marketing_batch_pipeline_poc',
    default_args=default_args,
    description='POC Batch Processing Pipeline (Branching & Retries)',
    schedule_interval=timedelta(days=1),
    start_date=datetime(2023, 1, 1),
    catchup=False,
    tags=['marketing', 'batch_processing'],
) as dag:

    start_pipeline = EmptyOperator(task_id='start')

    read_data = PythonOperator(
        task_id='read_data_source',
        python_callable=simulate_data_read,
    )

    branching = BranchPythonOperator(
        task_id='analyze_and_branch',
        python_callable=decide_branch,
    )

    process_high_quality = BashOperator(
        task_id='process_high_quality_data',
        bash_command='echo "Processing high quality data..." && sleep 2',
    )
    
    notify_success = BashOperator(
         task_id='send_success_email',
         bash_command='echo "Send success email notification"',
    )
    
    # Intentionally failing task to demonstrate retries and failure alerts
    handle_low_quality = BashOperator(
        task_id='handle_low_quality_data',
        bash_command='echo "Handling low quality data..." && sleep 2 && exit 1',
    )

    end_pipeline = EmptyOperator(
        task_id='end',
        trigger_rule='none_failed_min_one_success' 
    )

    # DAG definitions
    start_pipeline >> read_data >> branching
    branching >> process_high_quality >> notify_success >> end_pipeline
    branching >> handle_low_quality >> end_pipeline
