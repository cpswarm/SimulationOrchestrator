#!/usr/bin/env python

import rospy
import smach
import smach_ros
import mavros_msgs

from cpswarm_msgs.msg import *
from swarmros.msg import *


# define state Idle
class Idle(smach.State):

	def __init__(self):
		smach.State.__init__(self, outcomes=['succeeded', 'preempted'])

	def execute(self, userdata):
		rospy.loginfo('Executing state Idle')
		while True:
			# Check for preempt
			if self.preempt_requested():
				rospy.loginfo("Idle state has been preempted")
				self.service_preempt()
				return 'preempted'
			rospy.sleep(1.0)
			
		return 'succeeded'
		

def main():
	rospy.init_node('state_machine_node')
	
	# Create a TOP level SMACH state machine
	top_sm = smach.StateMachine(['succeeded', 'preempted', 'aborted'])
	
	# Open the container
	with top_sm:

		#  ===================================== SarThreads =====================================
		# Callback for custom outcomes from SarThreads #
		def out_cb(outcome_map):
			# To be completed
			return 'aborted'

		# Create a Concurrence container
		sarthreads_concurrence = smach.Concurrence(
			outcomes=['missionAbort', 'aborted'],
			default_outcome='missionAbort',
			child_termination_cb=lambda so: True,
			outcome_cb=out_cb)

		# Open the container
		with sarthreads_concurrence:

			# ===================================== SarBehavior =====================================
			# Create a State Machine container
			sarbehavior_sm = smach.StateMachine(
				outcomes=['succeeded', 'preempted', 'aborted'])

			# Open the container
			with sarbehavior_sm:

				#  ===================================== IdleThreads =====================================
				# Callback for custom outcomes from IdleThreads #
				def out_cb(outcome_map):
					# To be completed
					return 'aborted'

				# Create a Concurrence container
				idlethreads_concurrence = smach.Concurrence(
					outcomes=['missionStart', 'aborted'],
					default_outcome='missionStart',
					child_termination_cb=lambda so: True,
					outcome_cb=out_cb)

				# Open the container
				with idlethreads_concurrence:

					# ADD Idle to IdleThreads #
					smach.Concurrence.add('Idle',
						# NOT_SUPPORTED
					)

					# ADD IdleEventMonitoring to IdleThreads #
					smach.Concurrence.add('IdleEventMonitoring',
						smach_ros.MonitorState('bridge/events/mission_start',
							SimpleEvent,
							cond_cb=lambda ud, msg: False))
				#  ===================================== IdleThreads END =====================================

				# ADD IdleThreads to SarBehavior #
				smach.StateMachine.add('IdleThreads',
					idlethreads_concurrence,
					transitions={'missionStart':'TakeOff'})

				# ADD TakeOff to SarBehavior #
				smach.StateMachine.add('TakeOff',
					smach_ros.SimpleActionState('cmd/takeoff',
						TakeOffAction,
						goal=TakeOffGoal(1.5)),
					transitions={'succeeded':'Coverage'})

				# ADD Coverage to SarBehavior #
				smach.StateMachine.add('Coverage',
					smach_ros.SimpleActionState('uav_coverage',
						CoverageAction),
					transitions={'succeeded':'SelectRover'})

				# ADD SelectRover to SarBehavior #
				smach.StateMachine.add('SelectRover',
					smach_ros.SimpleActionState('cmd/assign_task',
						AssignTaskAction,
						goal_slots=['target_id', 'pose']),
					transitions={'succeeded':'Tracking', 'aborted':'SelectRover'})

				# ADD Tracking to SarBehavior #
				smach.StateMachine.add('Tracking',
					smach_ros.SimpleActionState('uav_tracking',
						TrackingAction,
						goal_slots=['target', 'cps']),
					transitions={'succeeded':'Coverage', 'aborted':'LocalCoverage'})

				# ADD LocalCoverage to SarBehavior #
				smach.StateMachine.add('LocalCoverage',
					smach_ros.SimpleActionState('uav_local_coverage',
						CoverageAction),
					transitions={'aborted':'Coverage', 'succeeded':'Tracking'})
			#  ===================================== SarBehavior END =====================================

			# ADD SarBehavior to SarThreads #
			smach.Concurrence.add('SarBehavior', sarbehavior_sm)

			# ADD AbortEventMonitoring to SarThreads #
			smach.Concurrence.add('AbortEventMonitoring',
				smach_ros.MonitorState('bridge/events/mission_abort',
					SimpleEvent,
					cond_cb=lambda ud, msg: False))
		#  ===================================== SarThreads END =====================================

		# ADD SarThreads to TOP state #
		smach.StateMachine.add('SarThreads',
			sarthreads_concurrence,
			transitions={'missionAbort':'MissionAbort'})

		# ===================================== MissionAbort =====================================
		# Create a State Machine container
		missionabort_sm = smach.StateMachine(
			outcomes=['succeeded'])

		# Open the container
		with missionabort_sm:

			# ADD Land to MissionAbort #
			smach.StateMachine.add('Land',
				smach_ros.ServiceState('cmd/land',
					std_srvs.srv.Empty),
				transitions={})
		#  ===================================== MissionAbort END =====================================

		# ADD MissionAbort to TOP state #
		smach.StateMachine.add('MissionAbort',
			missionabort_sm,
			transitions={'succeeded':'SarThreads'})
	
	# Create and start the introspection server (uncomment if needed)
	# sis = smach_ros.IntrospectionServer('smach_server', top_sm, '/SM_TOP')
	# sis.start()
	
	# Execute SMACH plan
	outcome = top_sm.execute()

	# Wait for ctrl-c to stop the application
	rospy.spin()
	# sis.stop()
	
	
if __name__ == '__main__':
	try:
		main()
	except rospy.ROSInterruptException:
		pass
